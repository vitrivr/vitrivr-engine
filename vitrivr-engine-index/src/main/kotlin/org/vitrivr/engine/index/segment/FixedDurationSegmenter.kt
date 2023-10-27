package org.vitrivr.engine.index.segment

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithRelationship
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter
import org.vitrivr.engine.core.source.Source
import java.time.Duration
import java.util.*

/**
 * Segments a content flow into segments of a specified target temporal duration.
 * Discards all non [SourcedContent.Temporal] content.
 */
class FixedDurationSegmenter(
    /** The input [Operator]. */
    input: Operator<ContentElement<*>>,

    /** The [Schema] being affected by this [FixedDurationSegmenter]. */
    schema: Schema,

    /** The target duration of the segments to be created */
    length: Duration,

    /** Size of the time window beyond the target duration to be considered for incoming content */
    lookAheadTime: Duration = Duration.ofSeconds(1)
) : AbstractSegmenter(input, schema) {

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


    override suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Ingested>) {
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
    override suspend fun finish(downstream: ProducerScope<Ingested>) {
        while (cache.isNotEmpty()) {
            sendFromCache(downstream)
        }
    }

    /**
     *
     */
    private suspend fun sendFromCache(downstream: ProducerScope<Ingested>) {
        val source = this.lastSource
        val nextStartTime = lastStartTime + lengthNanos
        require(source != null) { "Last source is null. This is a programmer's error!" }

        /* Generate source content. */
        val sourceRetrievable = object : Ingested, RetrievableWithSource {
            override val id: RetrievableId = source.sourceId
            override val type: String = "source"
            override val transient: Boolean = false
            override val source: Source = source
        }

        /* Persist source retrievable and send it downstream, if it doesn't exist. */
        if (!this.reader.exists(source.sourceId)) {
            this.writer.add(sourceRetrievable)
            downstream.send(sourceRetrievable)
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
        val retrievable = object : Ingested, RetrievableWithContent, RetrievableWithRelationship {
            override val id: RetrievableId = UUID.randomUUID()
            override val type: String = "segment"
            override val content: List<ContentElement<*>> = content
            override val transient: Boolean = false
            override val relationships: Map<String, List<Retrievable>> = mapOf(
                "partOf" to listOf(sourceRetrievable)
            )
        }

        /* Persist retrievable. */
        this.writer.add(retrievable)

        /* Send retrievable downstream. */
        downstream.send(retrievable)

        /* Update last start time. */
        this.lastStartTime = nextStartTime
    }
}