package org.vitrivr.engine.core.operators.ingest

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.util.*

/**
 * An abstract [Segmenter] implementation.
 *
 * It handles intricacies of merging the incoming [Flow] of [Content] and channeling it to a [SharedFlow].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractSegmenter(override val input: Operator<Content>): Segmenter {

    /** The [SharedFlow] returned by this [AbstractSegmenter]'s [toFlow] method. Is created lazily. */
    private var sharedFlow: SharedFlow<IngestedRetrievable>? = null


    /**
     * A special [IngestedRetrievable] that can be used to signal the termination of an ingest pipeline.
     */
    object TerminalIngestedRetrievable: IngestedRetrievable {
        override val id: RetrievableId = UUID.randomUUID()
        override val partOf: Set<Retrievable> = emptySet()
        override val parts: Set<Retrievable> = emptySet()
        override val transient: Boolean = true
        override val content: List<Content> = emptyList()
        override val descriptors: List<Descriptor> = emptyList()
        override fun addContent(content: Content) = throw UnsupportedOperationException("Cannot add content to TerminalIngestedRetrievable.")
        override fun addDescriptor(descriptor: Descriptor) = throw UnsupportedOperationException("Cannot add content to TerminalIngestedRetrievable.")
        override fun getDerivedContent(name: DerivateName) = throw UnsupportedOperationException("Cannot add content to TerminalIngestedRetrievable.")
    }

    /**
     * Implements the [AbstractSegmenter]'s [toFlow] method. Uses a shared channel flow for downstream communication
     * (broadcasting) and injects a [TerminalIngestedRetrievable], once the upstream flow completes.
     *
     * This acts as a signal, that the ingest pipeline has terminated.
     *
     * @return A [SharedFlow]
     */
    final override fun toFlow(scope: CoroutineScope): SharedFlow<IngestedRetrievable> {
        var existing = this.sharedFlow
        if (existing != null) return existing
        existing = channelFlow {
            val input = this@AbstractSegmenter.input.toFlow(scope).onCompletion {
                send(TerminalIngestedRetrievable)
            }
            this@AbstractSegmenter.segment(input, this)
        }.shareIn(scope, SharingStarted.Lazily, 0)
        this.sharedFlow = existing
        return existing
    }

    /**
     * Methods that implements segmentation.
     *
     * @param upstream The upstream [Flow] of [Content] that are being segmented.
     * @param downstream The [ProducerScope] to hand [IngestedRetrievable] to the downstream pipeline.
     */
    abstract suspend fun segment(upstream: Flow<Content>, downstream: ProducerScope<IngestedRetrievable>)
}