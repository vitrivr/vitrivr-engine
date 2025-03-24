package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
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
                    srcRetrievable = Ingested(UUID.randomUUID(), "SOURCE:VIDEO", emptyList(), emptySet(), emptySet(), emptySet(), true)
                }

                /* Final element of a single video. */
                if (ingested.type == "SOURCE:VIDEO") {
                    /* Send remaining segments in cache. */
                    sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos, srcRetrievable!!)

                    /* Send source retrievable. */
                    downstream.send(srcRetrievable!!.copy(content = ingested.content, descriptors = ingested.descriptors, attributes = ingested.attributes))
                    srcRetrievable = Ingested(UUID.randomUUID(), "SOURCE:VIDEO", emptyList(), emptySet(), emptySet(), emptySet(), true)
                    return@collect
                }

                val timestamp = ingested.filteredAttribute(TimeRangeAttribute::class.java) ?: return@collect
                val source = ingested.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect

                /* Check if source has changed. */
                if (lastSource != source) {
                    while (cache.isNotEmpty()) {
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

            /* Prepare new retrievable ID and collections. */
            val retrievableId = UUID.randomUUID()
            val content = mutableListOf<ContentElement<*>>()
            val descriptors = mutableSetOf<Descriptor<*>>()
            val relationships = mutableSetOf<Relationship>()
            val attributes = mutableSetOf<RetrievableAttribute>()

            /* Values to track time range. */
            var (min, max) = Long.MAX_VALUE to Long.MIN_VALUE

            for (emitted in emit) {
                emitted.content.forEach { content.add(it) }
                emitted.descriptors.forEach { descriptors.add(it) }
                emitted.relationships.forEach {
                    Relationship.ById(retrievableId, it.predicate, srcRetrievable.id, true).let {
                        relationships.add(it)
                    }
                }
                emitted.attributes.forEach {
                    it.takeUnless { it is TimeRangeAttribute }?.let { attributes.add(it) }
                    it.takeIf { it is TimeRangeAttribute }?.let {
                        min = (it as TimeRangeAttribute).takeIf { it.startNs < min }?.startNs ?: min
                        max = it.takeIf { it.endNs > max }?.endNs ?: max
                    }
                }
            }
            attributes.add(TimeRangeAttribute(min, max))

            /* Send retrievable downstream. */
            downstream.send(Ingested(retrievableId, emit.first().type, content, descriptors, attributes, relationships, true))
        }
    }
}