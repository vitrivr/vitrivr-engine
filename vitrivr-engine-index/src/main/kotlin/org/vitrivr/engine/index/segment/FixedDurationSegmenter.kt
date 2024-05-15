package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.TimePointAttribute
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
        val duration = Duration.ofSeconds(
            (context[name, "duration"] ?: throw IllegalArgumentException("Property 'duration' must be specified")).toLong()
        )
        val lookAheadTime = Duration.ofSeconds(
            (context[name, "lookAheadTime"] ?: throw IllegalArgumentException("Property 'lookAheadTime' must be specified")).toLong()
        )
        return Instance(input, duration, lookAheadTime)
    }

    /**
     * The [Transformer] returned by this [FixedDurationSegmenter].
     */
    private class Instance(
        /** The input [Operator]. */
        override val input: Operator<out Retrievable>,

        /** The target duration of the segments to be created */
        length: Duration,

        /** Size of the time window beyond the target duration to be considered for incoming content */
        lookAheadTime: Duration = Duration.ofSeconds(1)
    ) : Transformer {
        /** The desired target duration in ns. */
        private val lengthNanos = length.toNanos()

        /** The look-ahead time. */
        private val lookAheadNanos = lookAheadTime.toNanos()

        /** Cache of [SourcedContent.Temporal] elements. */
        private val cache = LinkedList<ContentElement<*>>()

        /**
         *
         */
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            val downstream = this

            /* Prepare necessary data structures. */
            var lastSource: Source? = null
            var lastStartTime = 0L
            val cache = LinkedList<Retrievable>()

            /* Collect upstream flow. */
            this@Instance.input.toFlow(scope).collect { ingested ->
                val timestamp = ingested.filteredAttribute(TimePointAttribute::class.java) ?: return@collect
                val source = ingested.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect

                /* Check if source has changed. */
                if (lastSource != source) {
                    while (this@Instance.cache.isNotEmpty()) {
                        sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos)
                        lastSource = source
                        lastStartTime = 0L
                    }
                }

                /* Add item to cache. */
                cache.add(ingested)

                /* Check if cut-off time has been exceeded. */
                val cutOffTime = lastStartTime + this@Instance.lengthNanos + this@Instance.lookAheadNanos
                if (timestamp.timepointNs >= cutOffTime) {
                    sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos)
                    lastStartTime += this@Instance.lengthNanos
                }
            }

            /* Drain remaining items in cache. */
            while (cache.isNotEmpty()) {
                sendFromCache(downstream, cache, lastStartTime + this@Instance.lengthNanos)
            }
        }

        /**
         *
         */
        private suspend fun sendFromCache(downstream: ProducerScope<Retrievable>, cache: LinkedList<Retrievable>, nextStartTime: Long) {
            /* Drain cache. */
            val emit = LinkedList<Retrievable>()
            cache.removeIf {
                val timestamp = it.filteredAttribute(TimePointAttribute::class.java) ?: return@removeIf true
                if (timestamp.timepointNs < nextStartTime) {
                    emit.add(it)
                    true
                } else {
                    false
                }
            }

            /* Prepare new ingested. */
            val ingested = Ingested(UUID.randomUUID(), emit.first().type, false)
            for (emitted in emit) {
                emitted.content.forEach { ingested.addContent(it) }
                emitted.descriptors.forEach { ingested.addDescriptor(it) }
                emitted.relationships.forEach { ingested.addRelationship(it) }
                emitted.attributes.forEach { ingested.addAttribute(it) }
            }

            /* Send retrievable downstream. */
            downstream.send(ingested)
        }
    }
}
