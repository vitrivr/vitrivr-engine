package org.vitrivr.engine.core.operators.ingest.templates

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.AbstractSegmenter
import java.util.*


private val logger: KLogger = KotlinLogging.logger {}

/**
 *
 *
 * @author
 * @version 1.0.0
 */
class DummySegmenter(input: Operator<ContentElement<*>>, schema: Schema, val parameters: Map<String, Any>) : AbstractSegmenter(input, schema) {

    /**
     * Segments by creating a [Ingested] for every incoming [Content] element, attaching that [Content] element to the [Ingested].
     *
     * @param upstream The upstream [Flow] of [Content] that is being segmented.
     * @param downstream The [ProducerScope] to hand [Ingested] to the downstream pipeline.
     */
    override suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Ingested>) = upstream.collect {
        val retrievable = object : Ingested {
            override val id: RetrievableId = UUID.randomUUID()
            override val type: String = "segment"
            override val transient: Boolean = false
        }
        downstream.send(retrievable)
        finish(downstream)
    }

    override suspend fun finish(downstream: ProducerScope<Ingested>) {
        logger.info { "Performed Dummy Segmenter with options ${parameters} on ${input}" }    }
}