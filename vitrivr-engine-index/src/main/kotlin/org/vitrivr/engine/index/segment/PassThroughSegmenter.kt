package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter

class PassThroughSegmenter(override val input: Operator<Content>, scope: CoroutineScope, private val retrievableWriter: RetrievableWriter) : Segmenter {

    override var emitted: Int = 0
        private set
    override var inputExhausted: Boolean = false
        private set

    private val sharedFlow: SharedFlow<IngestedRetrievable>

    init {
        val flow = this.input.toFlow().map {
            val retrievable = IngestedRetrievable.Default(
                transient = false,
                content = mutableListOf(it)
            )
            retrievableWriter.add(retrievable)
            ++emitted
            retrievable
        }.onCompletion {
            this@PassThroughSegmenter.inputExhausted = true
        }

        sharedFlow = flow.buffer(capacity = 1, onBufferOverflow = BufferOverflow.SUSPEND).shareIn(scope, SharingStarted.Lazily)
    }



    override fun toFlow(): SharedFlow<IngestedRetrievable> = this.sharedFlow
}