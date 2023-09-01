package org.vitrivr.engine.index.segment

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter

/**
 * An [AbstractSegmenter] that creates an [IngestedRetrievable] for every incoming [Content] element.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class PassThroughSegmenter(input: Operator<Content>, private val retrievableWriter: RetrievableWriter) : AbstractSegmenter(input) {

    /**
     * Segments by creating a [IngestedRetrievable] for every incoming [Content] element, attaching that [Content] element to the [IngestedRetrievable].
     *
     * @param upstream The upstream [Flow] of [Content] that is being segmented.
     * @param downstream The [ProducerScope] to hand [IngestedRetrievable] to the downstream pipeline.
     */
    override suspend fun segment(upstream: Flow<Content>, downstream: ProducerScope<IngestedRetrievable>) = upstream.collect {
        val retrievable = IngestedRetrievable.Default(transient = false, content = mutableListOf(it))
        this.retrievableWriter.add(retrievable)
        downstream.send(retrievable)
    }
}