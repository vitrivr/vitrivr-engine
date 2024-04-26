package org.vitrivr.engine.index.segment

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ContentAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import java.util.*

/**
 * An [AbstractSegmenter] that creates an [Ingested] for every incoming [Content] element.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 *
 * @version 1.1.0
 */
class PassThroughSegmenter : SegmenterFactory {
    private val logger: KLogger = KotlinLogging.logger {}
    /**
     * Creates a new [Segmenter] instance from this [PassThroughSegmenter].
     *
     * @param input The input [Transformer].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(input: Transformer, context: IndexContext, parameters: Map<String, String>): Segmenter = Instance(input, context)

    /**
     * Creates a new [Segmenter] instance from this [PassThroughSegmenter].
     *
     * @param input The input [Segmenter].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    override fun newOperator(input: Decoder, context: IndexContext, parameters: Map<String, String>): Segmenter = Instance(input, context)

    /**
     * The [AbstractSegmenter] returned by this [PassThroughSegmenter].
     */
    private class Instance(input: Operator<ContentElement<*>>, context: IndexContext) : AbstractSegmenter(input, context) {

        private val logger: KLogger = KotlinLogging.logger {}


        /**
         * Segments by creating a [Ingested] for every incoming [Content] element, attaching that [Content] element to the [Ingested].
         *
         * @param upstream The upstream [Flow] of [Content] that is being segmented.
         * @param downstream The [ProducerScope] to hand [Ingested] to the downstream pipeline.
         */
        override suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Retrievable>) = upstream.collect {
            /* Retrievable for data source. */
            if (it is SourcedContent) {
                val retrievable = Ingested(it.source.sourceId, "source:${it.source.type.toString().lowercase()}", false)
                retrievable.addAttribute(SourceAttribute(it.source))
                retrievable.addAttribute(ContentAttribute(it))

                /* Send retrievable downstream. */
                logger.debug { "In flow: Passing through downstream ${retrievable.id} with ${retrievable.type}" }
                downstream.send(retrievable)
            } else {
                val retrievable = Ingested(UUID.randomUUID(), null, false)
                retrievable.addAttribute(ContentAttribute(it))

                /* Send retrievable downstream. */
                logger.debug { "In flow: Passing through downstream ${retrievable.id} with ${retrievable.type}" }
                downstream.send(retrievable)
            }
        }

        override suspend fun finish(downstream: ProducerScope<Retrievable>) {
            //nothing left to do here
        }
    }
}