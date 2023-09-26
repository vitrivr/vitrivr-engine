package org.vitrivr.engine.index.segment

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter

/**
 * An [AbstractSegmenter] that creates an [Ingested] for every incoming [Content] element.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class PassThroughSegmenter(input: Operator<ContentElement<*>>, private val retrievableWriter: RetrievableWriter?) : AbstractSegmenter(input) {

    /**
     * Segments by creating a [Ingested] for every incoming [Content] element, attaching that [Content] element to the [Ingested].
     *
     * @param upstream The upstream [Flow] of [Content] that is being segmented.
     * @param downstream The [ProducerScope] to hand [Ingested] to the downstream pipeline.
     */
    override suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Ingested>) = upstream.collect {
        val retrievable = Ingested.Default(transient = false, type = "segment", content = mutableListOf(it))
        this.retrievableWriter?.add(retrievable)
        downstream.send(retrievable)
    }

    override suspend fun finish(downstream: ProducerScope<Ingested>) {
        //nothing left to do here
    }
}