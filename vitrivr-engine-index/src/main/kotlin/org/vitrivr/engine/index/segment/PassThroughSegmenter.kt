package org.vitrivr.engine.index.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter

class PassThroughSegmenter(override val input: Operator<Content>, private val scope: CoroutineScope) : Segmenter {

    var done = false
    var counter = 0
        private set

    private val sharedFlow: SharedFlow<IngestedRetrievable>

    init {
        val flow = this.input.toFlow().map {
            ++counter
            IngestedRetrievable.Default(
                transient = false,
                content = mutableListOf(it)
            )
        }

        sharedFlow = flow.onCompletion {
            println("DECODER DONE!")
            this@PassThroughSegmenter.done = true
        }.shareIn(scope, SharingStarted.Lazily)
    }

    override fun toFlow(): SharedFlow<IngestedRetrievable> = this.sharedFlow
}