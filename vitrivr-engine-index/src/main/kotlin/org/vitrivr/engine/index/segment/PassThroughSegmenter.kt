package org.vitrivr.engine.index.segment

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithSource
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.source.Source
import java.util.*

/**
 * An [AbstractSegmenter] that creates an [Ingested] for every incoming [Content] element.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class PassThroughSegmenter : SegmenterFactory {

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

        /**
         * Segments by creating a [Ingested] for every incoming [Content] element, attaching that [Content] element to the [Ingested].
         *
         * @param upstream The upstream [Flow] of [Content] that is being segmented.
         * @param downstream The [ProducerScope] to hand [Ingested] to the downstream pipeline.
         */
        override suspend fun segment(upstream: Flow<ContentElement<*>>, downstream: ProducerScope<Retrievable>) {
            var sourceRetrievable: RetrievableWithSource? = null
            upstream.collect {
                /* Retrievable for data source. */
                if (it is SourcedContent) {
                    if (it.source != sourceRetrievable?.source) {
                        val source = this.createRetrievableForSource(it.source)
                        downstream.send(source)
                        sourceRetrievable = source
                    }

                    /* Prepare retrievable (with relationship). */
                    val retrievable = Ingested(UUID.randomUUID(), "segment", false, content = listOf(it))
                    this.writer.add(retrievable)

                    /* Connect retrievable to source. */
                    sourceRetrievable?.let { source ->
                        retrievable.relationships.add(Relationship(retrievable, "partOf", source))
                        this.writer.connect(retrievable.id, "partOf", source.id)
                    }

                    /* Send retrievable downstream. */
                    downstream.send(retrievable)
                } else {
                    val retrievable = Ingested(UUID.randomUUID(), "segment", false, content = listOf(it))

                    /* Persist retrievable. */
                    this.writer.add(retrievable)

                    /* Send retrievable downstream. */
                    downstream.send(retrievable)
                }
            }
        }

        override suspend fun finish(downstream: ProducerScope<Retrievable>) {
            //nothing left to do here
        }

        /**
         * Tries to find a [Retrievable] for the provided [Source] (based on its [Source.sourceId])
         * and creates a new one, if it doesn't exist yet.
         *
         * @param source The [Source] to create [Retrievable] for.
         */
        private fun createRetrievableForSource(source: Source): RetrievableWithSource {
            val result = object : RetrievableWithSource {
                override val id: RetrievableId = source.sourceId
                override val type: String = "source"
                override val transient: Boolean = false
                override val source: Source = source
            }

            /* Persist retrievable. */
            this.writer.add(result)

            /* Return result. */
            return result
        }
    }
}