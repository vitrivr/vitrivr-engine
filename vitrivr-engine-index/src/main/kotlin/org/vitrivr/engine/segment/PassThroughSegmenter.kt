package org.vitrivr.engine.segment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter

class PassThroughSegmenter(override val input: Operator<Content>, private val scope: CoroutineScope) : Segmenter {

    override fun toFlow(): SharedFlow<IngestedRetrievable> {
        return this.input.toFlow().map {
            IngestedRetrievable.Default(
                transient = false,
                content = mutableListOf(it)
            )
        }.shareIn(scope, SharingStarted.Lazily)
    }
}