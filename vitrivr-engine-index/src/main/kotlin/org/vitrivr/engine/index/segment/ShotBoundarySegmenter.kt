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
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.index.util.boundaryFile.MediaSegmentDescriptor
import org.vitrivr.engine.index.util.boundaryFile.ShotBoundaryProvider
import org.vitrivr.engine.index.util.boundaryFile.ShotBoundaryProviderFactory
import java.nio.file.Path
import java.time.Duration
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.pathString

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
        val sbProvider = context[name, "sbProvider"]?.let { it }
            ?: throw IllegalArgumentException("ShotBoundaryProvider not specified for $name")
        val sbParams = context[name, "sbParams"]?.let { it }
            ?: throw IllegalArgumentException("ShotBoundaryProvider not specified for $name")
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
        return Instance(input, name, context, sbProvider, sbParams, tolerance, duration, lookAheadTime)
    }

    /**
     * The [Transformer] returned by this [FixedDurationSegmenter].
     */
    private class Instance(
        /** The input [Operator]. */
        override val input: Operator<out Retrievable>,

        override val name: String,

        context: Context,

        /** Path to folder of shotBoundary Files **/
        sbProvider: String,

        sbName: String,

        /** The target duration of the segments to be created */
        tolerance: Duration,

        /** The target duration of the segments to be created */
        length: Duration,

        /** Size of the time window beyond the target duration to be considered for incoming content */
        lookAheadTime: Duration = Duration.ofSeconds(1)
    ) : Transformer {

        val factory = loadServiceForName<ShotBoundaryProviderFactory>(sbProvider)
            ?: throw IllegalArgumentException("Failed to find ShotBoundaryProviderFactory for $name")
        private val sb: ShotBoundaryProvider = factory.newShotBoundaryProvider(sbName, context)

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
            var srcRetrievable: Retrievable? = null
            var sbs: List<MediaSegmentDescriptor>? = null
            var icSbs = 0

            /* Collect upstream flow. */
            this@Instance.input.toFlow(scope).collect { ingested ->

                if (srcRetrievable == null) {
                    srcRetrievable = Ingested(UUID.randomUUID(), "SOURCE:VIDEO", false)
                }

                if (ingested.type == "SOURCE:VIDEO") {
                    ingested.content.forEach { srcRetrievable!!.addContent(it) }
                    ingested.descriptors.forEach { srcRetrievable!!.addDescriptor(it) }
                    ingested.attributes.forEach { srcRetrievable!!.addAttribute(it) }
                    sendFromCache(
                        downstream,
                        cache,
                        sbs!!.last().startAbs.toNanos(),
                        sbs!!.last().endAbs.toNanos(),
                        tolerance,
                        srcRetrievable!!
                    )
                    downstream.send(srcRetrievable!!)
                    srcRetrievable = Ingested(UUID.randomUUID(), "SOURCE:VIDEO", false)
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
                        sendFromCache(
                            downstream,
                            cache,
                            sbs!![icSbs].startAbs.toNanos(),
                            sbs!![icSbs].endAbs.toNanos(),
                            tolerance,
                            srcRetrievable!!
                        )
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
                    sendFromCache(
                        downstream,
                        cache,
                        sbs!![icSbs].startAbs.toNanos(),
                        sbs!![icSbs].endAbs.toNanos(),
                        tolerance,
                        srcRetrievable!!
                    )
                    icSbs++
                }
            }

            /* Drain remaining items in cache. */
            while (cache.isNotEmpty()) {
                sendFromCache(downstream, cache, 0L, Long.MAX_VALUE, tolerance, srcRetrievable!!)
            }
        }

        /**
         *
         */
        private suspend fun sendFromCache(
            downstream: ProducerScope<Retrievable>,
            cache: LinkedList<Retrievable>,
            startTime: Long,
            endTime: Long,
            tolerance: Long,
            srcRetrievable: Retrievable,

            ) {
            /* Drain cache. */
            val emit = LinkedList<Retrievable>()
            cache.removeIf {
                val timestamp = it.filteredAttribute(TimeRangeAttribute::class.java) ?: return@removeIf true
                if (startTime <= timestamp.startNs && timestamp.endNs <= endTime) {
                    emit.add(it)
                    true
                } else {
                    false
                }
            }

            if (emit.isEmpty()) {
                return
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
                        max = (it as TimeRangeAttribute).takeIf { it.endNs > max }?.endNs ?: max
                    }
                }
            }
            ingested.addAttribute(TimeRangeAttribute(min, max))
            /* Send retrievable downstream. */
            downstream.send(ingested)
        }
    }
}