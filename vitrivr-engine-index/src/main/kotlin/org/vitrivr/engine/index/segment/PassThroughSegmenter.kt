package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter

class PassThroughSegmenter(override val input: Operator<Content>, private val scope: CoroutineScope) : Segmenter {

    override var emitted: Int = 0
        private set
    override var inputExhausted: Boolean = false
        private set

    private val sharedFlow: SharedFlow<IngestedRetrievable>

    init {
        val flow = this.input.toFlow().map {
            ++emitted
            IngestedRetrievable.Default(
                transient = false,
                content = mutableListOf(it)
            )
        }

        sharedFlow = flow.onCompletion {
            println("DECODER DONE!")
            this@PassThroughSegmenter.inputExhausted = true
        }.shareIn(scope, SharingStarted.Lazily)
    }



    override fun toFlow(): SharedFlow<IngestedRetrievable> = this.sharedFlow
}