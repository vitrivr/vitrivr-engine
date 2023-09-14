package org.vitrivr.engine.core.operators.ingest

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Operator
import java.util.*
import java.util.concurrent.locks.StampedLock

/**
 * An abstract [Segmenter] implementation.
 *
 * It handles intricacies of merging the incoming [Flow] of [Content] and channeling it to a [SharedFlow].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractSegmenter(override val input: Operator<ContentElement<*>>): Segmenter {

    /** The [SharedFlow] returned by this [AbstractSegmenter]'s [toFlow] method. Is created lazily. */
    private var sharedFlow: SharedFlow<Ingested>? = null

    private val lock = StampedLock()

    /**
     * A special [Ingested] that can be used to signal the termination of an ingest pipeline.
     */
    object TerminalIngestedRetrievable : Ingested {
        override val id: RetrievableId = UUID(0L, 0L)
        override val type: String? = null
        override val transient: Boolean = true
    }

    /**
     * Implements the [AbstractSegmenter]'s [toFlow] method. Uses a shared channel flow for downstream communication
     * (broadcasting) and injects a [TerminalIngestedRetrievable], once the upstream flow completes.
     *
     * This acts as a signal, that the ingest pipeline has terminated.
     *
     * @return A [SharedFlow]
     */
    final override fun toFlow(scope: CoroutineScope): SharedFlow<Ingested> {
        val stamp = this.lock.writeLock()
        try {
            if (this.sharedFlow != null) return this.sharedFlow!!
            this.sharedFlow = channelFlow {
                val input = this@AbstractSegmenter.input.toFlow(scope).onCompletion {
                    send(TerminalIngestedRetrievable)
                }
                this@AbstractSegmenter.segment(input, this)
            }.buffer(capacity = RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND).shareIn(CoroutineScope(scope.coroutineContext), SharingStarted.Lazily, 0)
            return this.sharedFlow!!
        } finally {
            this.lock.unlock(stamp)
        }
    }

    /**
     * Method that implements segmentation.
     *
     * @param upstream The upstream [Flow] of [Content] that are being segmented.
     * @param downstream The [ProducerScope] to hand [Ingested] to the downstream pipeline.
     */
    abstract suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Ingested>)

    /**
     * Method called to signify to the segmenter that the source is exhausted and no more content will be available.
     * Can be used to emit final segments.
     *
     * @param downstream The [ProducerScope] to hand [Ingested] to the downstream pipeline.
     */
    abstract suspend fun finish(downstream: ProducerScope<Ingested>)
}