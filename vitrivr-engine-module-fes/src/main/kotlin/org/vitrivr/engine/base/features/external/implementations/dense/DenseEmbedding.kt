package org.vitrivr.engine.base.features.external.implementations.dense

import org.vitrivr.engine.base.features.external.api.AbstractApi
import org.vitrivr.engine.base.features.external.api.ImageEmbeddingApi
import org.vitrivr.engine.base.features.external.api.TextEmbeddingApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.implementations.asr.ASR
import org.vitrivr.engine.base.features.external.implementations.asr.ASRExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.math.correspondence.BoundedCorrespondence
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser.Companion.merge
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityPredicate
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Implementation of the [DenseEmbedding] [ExternalFesAnalyser] that uses the [AbstractApi] to analyse image and text content.
 *
 * @author Fynn Faber
 * @version 1.1.0
 */
class DenseEmbedding : ExternalFesAnalyser<ContentElement<*>, FloatVectorDescriptor>() {

    companion object {
        const val LENGTH_PARAMETER_DEFAULT = 512
        const val LENGTH_PARAMETER_NAME = "length"
    }
    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [DenseEmbedding].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) : FloatVectorDescriptor {
        val length = field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull() ?: LENGTH_PARAMETER_DEFAULT
        return FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(length))
    }

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     * @return [DenseEmbeddingExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = DenseEmbeddingExtractor(input, name, this, context.local[name] ?: emptyMap())

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [FesExtractor] for.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     * @return [DenseEmbeddingExtractor]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext) = DenseEmbeddingExtractor(input, field, this, merge(field, context))

    /**
     * Generates and returns a new [DenseRetriever] instance for this [DenseEmbedding].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [DenseRetriever] instance for this [DenseEmbedding]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: Query, context: QueryContext) = DenseRetriever(field, query, context, BoundedCorrespondence(0.0f, 2.0f))

    /**
     * Generates and returns a new [DenseRetriever] instance for this [DenseEmbedding].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [DenseRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        /* Prepare query parameters. */
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        val timeoutSeconds = field.parameters[TIMEOUT_MS_PARAMETER_NAME]?.toLongOrNull() ?: TIMEOUT_MS_PARAMETER_DEFAULT
        val pollingIntervalMs = field.parameters[POLLINGINTERVAL_MS_PARAMETER_NAME]?.toLongOrNull() ?: POLLINGINTERVAL_MS_PARAMETER_DEFAULT
        val retries = field.parameters[RETRIES_PARAMETER_NAME]?.toIntOrNull() ?: RETRIES_PARAMETER_DEFAULT
        val model =  field.parameters[MODEL_PARAMETER_NAME] ?: throw IllegalStateException("Model parameter not set.")
        val k = context.getProperty(field.fieldName, QueryContext.LIMIT_KEY)?.toLongOrNull() ?: QueryContext.LIMIT_DEFAULT
        val fetchVector = context.getProperty(field.fieldName, QueryContext.FETCH_DESCRIPTOR_KEY)?.toBooleanStrictOrNull() == true

        /* Generate vector for content element. */
        val vector = when (val c = content.first { it is ImageContent || it is TextContent }) {
            is ImageContent -> ImageEmbeddingApi(host, model, timeoutSeconds, pollingIntervalMs, retries).analyse(c)
            is TextContent -> TextEmbeddingApi(host, model, timeoutSeconds, pollingIntervalMs, retries).analyse(c)
            else -> throw IllegalArgumentException("Unsupported content type ${c.javaClass.simpleName}.")
        }
        if (vector == null) {
            throw IllegalStateException("Failed to embed provided content.")
        }
        val predicate = ProximityPredicate(field = field, value = vector, k = k, fetchVector = fetchVector)

        /* Return retriever. */
        return this.newRetrieverForQuery(field, Query(predicate), context)
    }
}