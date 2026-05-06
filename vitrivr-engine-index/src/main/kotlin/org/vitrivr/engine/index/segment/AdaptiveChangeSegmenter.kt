package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.index.segment.signal.ClipSegmentationSignal
import org.vitrivr.engine.index.segment.signal.SegmentationSignal
import org.vitrivr.engine.index.segment.signal.SignalValue
import java.time.Duration
import java.util.LinkedList
import java.util.UUID

/**
 * Adaptive temporal segmenter based on temporary, non-persisted segmentation signals.
 *
 * It creates its own SOURCE:VIDEO retrievable and sends generated segment retrievables downstream.
 * Descriptor operators such as metadata/time/clip must still run after PersistRetrievableTransformer.
 *
 * @author Rahel Arnold
 */
class AdaptiveChangeSegmenter : OperatorFactory {

    override fun newOperator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        context: Context
    ): Operator<out Retrievable> {
        require(inputs.size == 1) {
            "The ${this::class.simpleName} only supports one input operator."
        }

        val minDuration = Duration.ofMillis(
            (context[name, "minDuration"]
                ?: throw IllegalArgumentException("Property 'minDuration' must be specified")).toLong()
        )
        val maxDuration = Duration.ofMillis(
            (context[name, "maxDuration"]
                ?: throw IllegalArgumentException("Property 'maxDuration' must be specified")).toLong()
        )
        val sampleInterval = Duration.ofMillis(
            (context[name, "sampleInterval"] ?: context[name, "minDuration"]
            ?: throw IllegalArgumentException("Property 'sampleInterval' must be specified")).toLong()
        )
        val threshold = (context[name, "threshold"]
            ?: throw IllegalArgumentException("Property 'threshold' must be specified")).toDouble()

        require(minDuration.toNanos() > 0) { "minDuration must be > 0" }
        require(maxDuration.toNanos() >= minDuration.toNanos()) { "maxDuration must be >= minDuration" }
        require(sampleInterval.toNanos() > 0) { "sampleInterval must be > 0" }

        val signalNames = (context[name, "signals"] ?: "clip")
            .split(',')
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        val signals = signalNames.map { signalName ->
            when (signalName) {
                "clip" -> ClipSegmentationSignal.fromContext(name, context)
                else -> throw IllegalArgumentException("Unsupported segmentation signal '$signalName'.")
            }
        }

        require(signals.isNotEmpty()) { "At least one segmentation signal must be configured." }

        return Instance(
            name = name,
            input = inputs.values.first(),
            minDuration = minDuration,
            maxDuration = maxDuration,
            sampleInterval = sampleInterval,
            threshold = threshold,
            signals = signals
        )
    }

    private class Instance(
        override val name: String,
        override val input: Operator<out Retrievable>,
        minDuration: Duration,
        maxDuration: Duration,
        sampleInterval: Duration,
        private val threshold: Double,
        private val signals: List<SegmentationSignal>
    ) : Transformer {

        private val minDurationNanos = minDuration.toNanos()
        private val maxDurationNanos = maxDuration.toNanos()
        private val sampleIntervalNanos = sampleInterval.toNanos()

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            val downstream = this

            var lastSource: Source? = null
            var segmentStartTime = 0L
            var lastProbeTime = Long.MIN_VALUE
            var previousState: Map<String, SignalValue> = emptyMap()
            val cache = LinkedList<Retrievable>()
            var srcRetrievable: Retrievable? = null

            this@Instance.input.toFlow(scope).collect { ingested ->
                if (srcRetrievable == null) {
                    srcRetrievable = newSourceRetrievable()
                }

                if (ingested.type == "SOURCE:VIDEO") {
                    while (cache.isNotEmpty()) {
                        sendFromCache(downstream, cache, Long.MAX_VALUE, srcRetrievable!!)
                    }

                    downstream.send(
                        srcRetrievable!!.copy(
                            content = ingested.content,
                            descriptors = ingested.descriptors,
                            attributes = ingested.attributes
                        )
                    )

                    srcRetrievable = newSourceRetrievable()
                    lastSource = null
                    segmentStartTime = 0L
                    lastProbeTime = Long.MIN_VALUE
                    previousState = emptyMap()
                    return@collect
                }

                val timestamp = ingested.filteredAttribute(TimeRangeAttribute::class.java)
                    ?: return@collect

                val source = ingested.filteredAttribute(SourceAttribute::class.java)?.source

                if (source != null && lastSource != null && lastSource != source) {
                    while (cache.isNotEmpty()) {
                        sendFromCache(downstream, cache, Long.MAX_VALUE, srcRetrievable!!)
                    }

                    srcRetrievable = newSourceRetrievable()
                    segmentStartTime = timestamp.startNs
                    lastProbeTime = Long.MIN_VALUE
                    previousState = emptyMap()
                }

                if (source != null) {
                    lastSource = source
                }

                if (cache.isEmpty()) {
                    segmentStartTime = timestamp.startNs
                }

                cache.add(ingested)
                val elapsed = timestamp.endNs - segmentStartTime

                if (elapsed >= maxDurationNanos) {
                    sendFromCache(downstream, cache, timestamp.endNs, srcRetrievable!!)
                    segmentStartTime = timestamp.endNs
                    lastProbeTime = timestamp.endNs
                    previousState = extractState(ingested)
                    return@collect
                }

                if (lastProbeTime == Long.MIN_VALUE || timestamp.endNs - lastProbeTime >= sampleIntervalNanos) {
                    val currentState = extractState(ingested)

                    if (elapsed >= minDurationNanos && previousState.isNotEmpty() && currentState.isNotEmpty()) {
                        val change = weightedChange(previousState, currentState)
                        if (change >= threshold) {
                            sendFromCache(downstream, cache, timestamp.startNs, srcRetrievable!!)
                            segmentStartTime = timestamp.startNs
                        }
                    }

                    if (currentState.isNotEmpty()) {
                        previousState = currentState
                        lastProbeTime = timestamp.endNs
                    }
                }
            }

            while (cache.isNotEmpty()) {
                sendFromCache(downstream, cache, Long.MAX_VALUE, srcRetrievable!!)
            }
        }

        private suspend fun extractState(retrievable: Retrievable): Map<String, SignalValue> = signals
            .mapNotNull { signal -> signal.extract(retrievable)?.let { signal.name to it } }
            .toMap()

        private fun weightedChange(
            previous: Map<String, SignalValue>,
            current: Map<String, SignalValue>
        ): Double {
            var weightedSum = 0.0
            var totalWeight = 0.0
            for (signal in signals) {
                val a = previous[signal.name]
                val b = current[signal.name]
                if (a != null && b != null) {
                    weightedSum += signal.weight * signal.distance(a, b)
                    totalWeight += signal.weight
                }
            }
            return if (totalWeight == 0.0) 0.0 else weightedSum / totalWeight
        }

        private fun newSourceRetrievable(): Retrievable = Ingested(
            UUID.randomUUID(),
            "SOURCE:VIDEO",
            emptyList(),
            emptySet(),
            emptySet(),
            emptySet(),
            false
        )

        private suspend fun sendFromCache(
            downstream: ProducerScope<Retrievable>,
            cache: LinkedList<Retrievable>,
            nextStartTime: Long,
            srcRetrievable: Retrievable
        ) {
            val emit = LinkedList<Retrievable>()

            cache.removeIf {
                val timestamp = it.filteredAttribute(TimeRangeAttribute::class.java)
                    ?: return@removeIf true
                if (timestamp.endNs <= nextStartTime) {
                    emit.add(it)
                    true
                } else {
                    false
                }
            }

            if (emit.isEmpty()) return

            val retrievableId = UUID.randomUUID()
            val content = mutableListOf<ContentElement<*>>()
            val descriptors = mutableSetOf<Descriptor<*>>()
            val relationships = mutableSetOf<Relationship>()
            val attributes = mutableSetOf<RetrievableAttribute>()

            var min = Long.MAX_VALUE
            var max = Long.MIN_VALUE

            for (emitted in emit) {
                emitted.content.forEach { content.add(it) }
                emitted.descriptors.forEach { descriptors.add(it) }
                emitted.relationships.forEach {
                    relationships.add(
                        Relationship.ById(
                            retrievableId,
                            it.predicate,
                            srcRetrievable.id,
                            false
                        )
                    )
                }
                emitted.attributes.forEach {
                    if (it is TimeRangeAttribute) {
                        if (it.startNs < min) min = it.startNs
                        if (it.endNs > max) max = it.endNs
                    } else {
                        attributes.add(it)
                    }
                }
            }

            attributes.add(TimeRangeAttribute(min, max))
            downstream.send(
                Ingested(
                    retrievableId,
                    emit.first().type,
                    content,
                    descriptors,
                    attributes,
                    relationships,
                    false
                )
            )
        }
    }
}
