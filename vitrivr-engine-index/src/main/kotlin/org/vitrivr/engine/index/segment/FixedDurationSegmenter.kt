package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.metadata.source.exif.logger
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimePointAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimeRangeAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.core.source.Source
import java.time.Duration
import java.util.*

/**
 * Segments a content flow into segments of a specified target temporal duration.
 * Discards all non [SourcedContent.Temporal] content.
 */
class FixedDurationSegmenter : TransformerFactory {
    /**
     * Creates a new [Transformer] instance from this [FixedDurationSegmenter].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator].
     * @param context The [Context] to use.
     */
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        val duration = Duration.ofMillis(
            (context[name, "duration"]
                ?: throw IllegalArgumentException("Property 'duration' must be specified")).toLong()
        )
        val lookAheadTime = Duration.ofMillis(
            (context[name, "lookAheadTime"]
                ?: throw IllegalArgumentException("Property 'lookAheadTime' must be specified")).toLong()
        )
        return Instance(input, name, duration, lookAheadTime)
    }

    /**
     * The [Transformer] returned by this [FixedDurationSegmenter].
     */
    private class Instance(
        /** The input [Operator]. */
        override val input: Operator<out Retrievable>,

        override val name: String,

        /** The target duration of the segments to be created */
        length: Duration,

        /** Size of the time window beyond the target duration to be considered for incoming content */
        lookAheadTime: Duration = Duration.ofSeconds(1)
    ) : Transformer {
        /** The desired target duration in ns. */
        private val lengthNanos = length.toNanos()

        /** The look-ahead time. */
        private val lookAheadNanos = lookAheadTime.toNanos()


        /**
         *
         */
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            val downstream = this

            /* Prepare necessary data structures. */
            var lastSource: Source? = null
            var lastStartTime = 0L
            val cache = LinkedList<Retrievable>()
            var srcRetrievable: Retrievable? = null

            /* Collect upstream flow. */
            this@Instance.input.toFlow(scope).collect { ingested ->

                if (srcRetrievable == null) {
                    srcRetrievable = Ingested(UUID.randomUUID(), "SOURCE:VIDEO", false)
                }

                if (ingested.type == "SOURCE:VIDEO") {
                    ingested.content.forEach { srcRetrievable!!.addContent(it) }
                    ingested.descriptors.forEach { srcRetrievable!!.addDescriptor(it) }
                    ingested.attributes.forEach { srcRetrievable!!.addAttribute(it) }
                    sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos, srcRetrievable!!)
                    downstream.send(srcRetrievable!!)
                    srcRetrievable = Ingested(UUID.randomUUID(), "SOURCE:VIDEO", false)
                    return@collect
                }

                val timestamp = ingested.filteredAttribute(TimeRangeAttribute::class.java) ?: return@collect
                val source = ingested.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect

                /* Check if source has changed. */
                if (lastSource != source) {
                    while (this@Instance.cache.isNotEmpty()) {
                        sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos, srcRetrievable!!)
                        lastSource = source
                        lastStartTime = 0L
                    }
                }

                /* Add item to cache. */
                cache.add(ingested)

                /* Check if cut-off time has been exceeded. */
                val cutOffTime = lastStartTime + this@Instance.lengthNanos + this@Instance.lookAheadNanos
                if (timestamp.endNs >= cutOffTime) {
                    sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos, srcRetrievable!!)
                    lastStartTime += this@Instance.lengthNanos
                }
            }

            /* Drain remaining items in cache. */
            while (cache.isNotEmpty()) {
                sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos, srcRetrievable!!)
            }
        }

        /**
         *
         */
        private suspend fun sendFromCache(
            downstream: ProducerScope<Retrievable>,
            cache: LinkedList<Retrievable>,
            nextStartTime: Long,
            srcRetrievable: Retrievable
        ) {
            /* Drain cache. */
            val emit = LinkedList<Retrievable>()
            cache.removeIf {
                val timestamp = it.filteredAttribute(TimeRangeAttribute::class.java) ?: return@removeIf true
                if (timestamp.endNs <= nextStartTime) {
                    emit.add(it)
                    true
                } else {
                    false
                }
            }

            /* Prepare new ingested. */
            val ingested = Ingested(UUID.randomUUID(), emit.first().type, false)
            var (min, max) = Long.MAX_VALUE to Long.MIN_VALUE

            for (emitted in emit) {
                emitted.content.forEach { ingested.addContent(it) }
                emitted.descriptors.forEach { ingested.addDescriptor(it) }
                emitted.relationships.forEach {
                    Relationship.BySubRefObjId(ingested, it.predicate, srcRetrievable.id, false).let {
                        ingested.addRelationship(it)
                        srcRetrievable.addRelationship(it)
                    }
                }
                emitted.attributes.forEach {
                    it.takeUnless { it is TimeRangeAttribute }?.let { ingested.addAttribute(it) }
                    it.takeIf { it is TimeRangeAttribute }?.let {
                        min = (it as TimeRangeAttribute).takeIf { it.startNs < min }?.startNs ?: min
                        max = (it as TimeRangeAttribute).takeIf { it.endNs > max }?.startNs ?: max
                    }
                }
            }
            ingested.addAttribute(TimeRangeAttribute(min, max))
            /* Send retrievable downstream. */
            downstream.send(ingested)
        }
    }
}