package org.vitrivr.engine.index.segment

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.source.Source
import java.time.Duration
import java.util.*

/**
 * Segments a content flow into segments of a specified target temporal duration.
 * Discards all non [SourcedContent.Temporal] content.
 */
class FixedDurationSegmenter : SegmenterFactory {

    /**
     * The [AbstractSegmenter] returned by this [FixedDurationSegmenter].
     */
    private fun internalNewOperator(input: Operator<ContentElement<*>>, context: IndexContext, parameters: Map<String, Any> = emptyMap()): Segmenter {
        val duration = Duration.ofSeconds(
            (parameters["duration"] as String? ?: throw IllegalArgumentException("'duration' must be specified")).toLong()
        )
        val lookAheadTime = Duration.ofSeconds(
            (parameters["lookAheadTime"] as String? ?: throw IllegalArgumentException("'lookAheadTime' must be specified")).toLong()
        )
        return Instance(input, context, duration, lookAheadTime)
    }

    /**
     * Creates a new [Segmenter] instance from this [SegmenterFactory].
     *
     * @param input The input [Transformer].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(input: Transformer, context: IndexContext, parameters: Map<String, String>): Segmenter = internalNewOperator(input, context, parameters)

    /**
     * Creates a new [Segmenter] instance from this [SegmenterFactory].
     *
     * @param input The input [Segmenter].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(input: Decoder, context: IndexContext, parameters: Map<String, String>): Segmenter = internalNewOperator(input, context, parameters)

    /**
     * The [AbstractSegmenter] returned by this [FixedDurationSegmenter].
     */
    private class Instance(
        /** The input [Operator]. */
        input: Operator<ContentElement<*>>,

        /** The [IndexContext] used by this [Instance]. */
        context: IndexContext,

        /** The target duration of the segments to be created */
        length: Duration,

        /** Size of the time window beyond the target duration to be considered for incoming content */
        lookAheadTime: Duration = Duration.ofSeconds(1)
    ) : AbstractSegmenter(input, context) {

        /** A [Mutex] to make sure, that only a single thread enters the critical section of this [FixedDurationSegmenter]. */
        private val mutex = Mutex()

        /** The desired target duration in ns. */
        private val lengthNanos = length.toNanos()

        /** The look-ahead time. */
        private val lookAheadNanos = lookAheadTime.toNanos()

        /** Cache of [SourcedContent.Temporal] elements. */
        private val cache = LinkedList<ContentElement<*>>()

        /** The last start timestamp encounterd by this [FixedDurationSegmenter]. */
        private var lastStartTime: Long = 0

        /** Reference to the last [Source] encountered by this [FixedDurationSegmenter]. */
        private var lastSource: Source? = null

        /** Tracks if the current source retrievable has already been persisted. */
        private var sourceWritten = false


        /** [KLogger] instance. */
        private val logger: KLogger = KotlinLogging.logger {}

        override suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Retrievable>) {
            upstream.collect { content ->
                this.mutex.lock()
                try {
                    if (content is SourcedContent.Temporal) {
                        if (content.source != this.lastSource) {
                            while (this.cache.isNotEmpty()) {
                                sendFromCache(downstream)
                            }
                            this.lastSource = content.source
                            this.lastStartTime = 0
                            this.sourceWritten = false
                            logger.info { "Starting to segment new source ${lastSource?.name} (${lastSource?.sourceId})" }
                        }
                        this.cache.add(content)
                        val cutOffTime = this.lastStartTime + this.lengthNanos + this.lookAheadNanos
                        if (content.timepointNs >= cutOffTime) {
                            sendFromCache(downstream)
                        }
                    }
                } finally {
                    this.mutex.unlock()
                }
            }
        }

        /**
         * Finishes the segmentation process by sending all remaining content from the cache.
         */
        override suspend fun finish(downstream: ProducerScope<Retrievable>) {
            while (cache.isNotEmpty()) {
                sendFromCache(downstream)
            }
        }

        /**
         *
         */
        private suspend fun sendFromCache(downstream: ProducerScope<Retrievable>) {
            val source = this.lastSource
            val nextStartTime = lastStartTime + lengthNanos
            require(source != null) { "Last source is null. This is a programmer's error!" }

            /* Generate source content. */
            val sourceRetrievable = object : RetrievableWithSource {
                override val id: RetrievableId = source.sourceId
                override val type: String = "source"
                override val transient: Boolean = false
                override val source: Source = source
            }

            /* Persist source retrievable and send it downstream */
            if (!this.sourceWritten) {
                this.writer.add(sourceRetrievable)
                downstream.send(sourceRetrievable)
                this.sourceWritten = true
            }


            /* Drain cache. */
            val content = LinkedList<ContentElement<*>>()
            this.cache.removeIf {
                require(it is SourcedContent.Temporal) { "Cache contains non-temporal content. This is a programmer's error!" }
                if (it.timepointNs < nextStartTime) {
                    content.add(it)
                    true
                } else {
                    false
                }
            }

            /* Prepare retrievable. */
            val retrievable = Ingested(UUID.randomUUID(), "segment", false, content, emptyList())
            retrievable.relationships.add(Relationship(retrievable, "partOf", sourceRetrievable))

            /* Persist retrievable and relationship. */
            this.writer.add(retrievable)
            this.writer.connect(retrievable.id, "partOf", sourceRetrievable.id)

            /* Send retrievable downstream. */
            downstream.send(retrievable)

            /* Update last start time. */
            this.lastStartTime = nextStartTime
        }
    }
}