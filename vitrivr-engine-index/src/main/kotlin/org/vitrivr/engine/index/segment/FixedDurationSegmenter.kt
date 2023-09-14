package org.vitrivr.engine.index.segment

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter
import java.time.Duration
import java.util.concurrent.locks.StampedLock

/**
 * Segments a content flow into segments of a specified target temporal duration.
 * Discards all non [SourcedContent.Temporal] content.
 */
class FixedDurationSegmenter(
    input: Operator<ContentElement<*>>,
    private val retrievableWriter: RetrievableWriter?,
    /** The target duration of the segments to be created */
    length: Duration,
    /** Size of the time window beyond the target duration to be considered for incoming content */
    lookAheadTime: Duration = Duration.ofSeconds(1)

) : AbstractSegmenter(input) {

    private val lock = StampedLock()
    private val lengthNanos = length.toNanos()
    private val lookAheadNanos = lookAheadTime.toNanos()
    private val cache = ArrayList<ContentElement<*>>()
    private var lastStartTime = 0L


    override suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Ingested>) {

        upstream.collect { content ->
            val stamp = this.lock.writeLock()
            val nextStartTime = lastStartTime + lengthNanos
            val cutOffTime = nextStartTime + lookAheadNanos
            try {
                if (content is SourcedContent.Temporal) {
                    cache.add(content)
                    if (content.timepointNs >= cutOffTime) {
                        sendFromCache(downstream)
                    }
                }
            } finally {
                this.lock.unlock(stamp)
            }
        }
    }

    override suspend fun finish(downstream: ProducerScope<Ingested>) {
        while (cache.isNotEmpty()) {
            sendFromCache(downstream)
        }
    }

    private suspend fun sendFromCache(downstream: ProducerScope<Ingested>) {
        val nextStartTime = lastStartTime + lengthNanos
        val nextSegmentContent = mutableListOf<ContentElement<*>>()
        cache.removeIf {
            require(it is SourcedContent.Temporal) { "Cache contains non.temporal content. This is a programmer's error!" }
            if (it.timepointNs < nextStartTime) {
                nextSegmentContent.add(it)
                true
            } else {
                false
            }
        }
        val retrievable = Ingested.Default(transient = false, content = nextSegmentContent, type = "segment")
        this.retrievableWriter?.add(retrievable)
        downstream.send(retrievable)
        this.lastStartTime = nextStartTime
    }


}