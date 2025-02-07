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
import org.vitrivr.engine.index.util.boundaryFile.BoundaryFileDecoder
import org.vitrivr.engine.index.util.boundaryFile.MediaSegmentDescriptor
import java.nio.file.Path
import java.time.Duration
import java.util.*
import kotlin.io.path.Path

/**
 * Segments a content flow into segments of a specified target temporal duration.
 * Discards all non [SourcedContent.Temporal] content.
 */
class ShotBoundarySegmenter : TransformerFactory {
    /**
     * Creates a new [Transformer] instance from this [FixedDurationSegmenter].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator].
     * @param context The [Context] to use.
     */
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        /** Path to folder of shotBoundary Files **/
        val sbPath = Path(
            (context[name, "sbPath"]
                ?: throw IllegalArgumentException("Property 'duration' must be specified"))
        )
        val tolerance = Duration.ofMillis(
            (context[name, "tolerance"]
                ?: throw IllegalArgumentException("Property 'duration' must be specified")).toLong()
        )
        val duration = Duration.ofMillis(
            (context[name, "duration"]
                ?: throw IllegalArgumentException("Property 'duration' must be specified")).toLong()
        )
        val lookAheadTime = Duration.ofMillis(
            (context[name, "lookAheadTime"]
                ?: throw IllegalArgumentException("Property 'lookAheadTime' must be specified")).toLong()
        )
        return Instance(input, name, sbPath, tolerance, duration, lookAheadTime)
    }

    /**
     * The [Transformer] returned by this [FixedDurationSegmenter].
     */
    private class Instance(
        /** The input [Operator]. */
        override val input: Operator<out Retrievable>,

        override val name: String,

        /** Path to folder of shotBoundary Files **/
        sbPath: Path,

        /** The target duration of the segments to be created */
        tolerance: Duration,

        /** The target duration of the segments to be created */
        length: Duration,

        /** Size of the time window beyond the target duration to be considered for incoming content */
        lookAheadTime: Duration = Duration.ofSeconds(1)
    ) : Transformer {

        private val sb: BoundaryFileDecoder = BoundaryFileDecoder(sbPath)

        private val tolerance: Long = tolerance.toNanos()

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

            val cache = LinkedList<Retrievable>()
            var sbs: List<MediaSegmentDescriptor>? = null
            var icSbs = 0

            /* Collect upstream flow. */
            this@Instance.input.toFlow(scope).collect { ingested ->

                /* Check if content is of type SOURCE:VIDEO; if so - empty cache end mit. */
                if (ingested.type == "SOURCE:VIDEO") {
                    sendFromCache(downstream, cache, sbs!!.last().startAbs.toNanos(), sbs!!.last().endAbs.toNanos(), tolerance)
                    downstream.send(ingested)
                    return@collect
                }

                val timestamp = ingested.filteredAttribute(TimeRangeAttribute::class.java) ?: return@collect
                val source = ingested.filteredAttribute(SourceAttribute::class.java)?.source ?: return@collect

                if (sbs == null) {
                    sbs = sb.decode(source.name.split(".")[0])
                    icSbs = 0
                }

                /* Check if source has changed. */
                if (lastSource != source) {
                    while (this@Instance.cache.isNotEmpty()) {
                        sendFromCache(downstream, cache, sbs!![icSbs].startAbs.toNanos(), sbs!![icSbs].endAbs.toNanos(), tolerance)
                    }
                    lastSource = source
                    sbs = sb.decode(source.name.split(".")[0])
                    icSbs = 0
                }

                /* Add item to cache. */
                cache.add(ingested)

                /* Check if cut-off time has been exceeded. */
                val cutOffTime = sbs!![icSbs].endAbs.toNanos() + this@Instance.lookAheadNanos
                if (timestamp.endNs >= cutOffTime) {
                    sendFromCache(downstream, cache, sbs!![icSbs].startAbs.toNanos(), sbs!![icSbs].endAbs.toNanos(), tolerance)
                    icSbs++
                }
            }

            /* Drain remaining items in cache. */
            while (cache.isNotEmpty()) {
                sendFromCache(downstream, cache, 0L, Long.MAX_VALUE, tolerance)
            }
        }

        /**
         * Sends a new [Retrievable] object downstream based on the content in the cache.
         *
         * @param downstream [ProducerScope] to use for sending the [Retrievable] object.
         * @param cache The [LinkedList] cache of [Retrievable]
         * @param startTime The start time of the next segment.
         * @param endTime The end time of the next segment.
         * @param tolerance The time tolerance.
         */
        private suspend fun sendFromCache(downstream: ProducerScope<Retrievable>, cache: LinkedList<Retrievable>, startTime: Long, endTime: Long, tolerance: Long) {
            /* Sanity check for early abort. */
            if (cache.isEmpty()) return

            /* Drain cache. */
            val emittedTypes = LinkedHashSet<String>()
            val emittedContent = LinkedList<ContentElement<*>>()
            val emittedDescriptors = HashSet<Descriptor<*>>()
            val emittedAttribute = HashSet<RetrievableAttribute>()
            val emittedRelationships = HashSet<Relationship>()
            var (min, max) = Long.MAX_VALUE to Long.MIN_VALUE
            cache.removeIf {
                val timestamp = it.filteredAttribute(TimeRangeAttribute::class.java) ?: return@removeIf true
                if (startTime <= timestamp.startNs && timestamp.endNs <= endTime) {
                    min = timestamp.takeIf { it.startNs < min }?.startNs ?: min
                    max = timestamp.takeIf { it.endNs > max }?.endNs ?: max
                    emittedTypes.add(it.type)
                    emittedContent.addAll(it.content)
                    emittedDescriptors.addAll(it.descriptors)
                    emittedAttribute.addAll(it.attributes)
                    emittedRelationships.addAll(it.relationships)
                    true
                } else {
                    false
                }
            }

            /* Emit new ingested. */
            downstream.send(Ingested(UUID.randomUUID(), emittedTypes.first, emittedContent, emittedDescriptors, emittedAttribute, emittedRelationships, transient = false))
        }
    }
}