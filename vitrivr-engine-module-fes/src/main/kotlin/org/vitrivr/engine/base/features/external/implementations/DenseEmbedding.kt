package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.dense.DenseRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Implementation of the [DenseEmbedding] [ExternalFesAnalyser] that uses the [ApiWrapper] to analyse image and text content.
 *
 * @author Fynn Faber
 * @version 1.1.0
 */
class DenseEmbedding : ExternalFesAnalyser<ContentElement<*>, FloatVectorDescriptor>() {

    companion object {
        const val LENGTH_PARAMETER_DEFAULT = 512
        const val LENGTH_PARAMETER_NAME = "length"
    }

    override val model = "clip-vit-large-patch14"
    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [DenseEmbedding].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [FloatVectorDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>) : FloatVectorDescriptor {
        //convert to integer
        val length = field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull() ?: LENGTH_PARAMETER_DEFAULT
        return FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(length))
    }

    /**
     * Generates and returns a new [DenseRetriever] instance for this [DenseEmbedding].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [DenseRetriever] instance for this [DenseEmbedding]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is ProximityQuery<*> && query.value is Value.FloatVector) { "The query is not a ProximityQuery<Value.FloatVector>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(field, query as ProximityQuery<Value.FloatVector>, context)
    }

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
        val timeoutSeconds = field.parameters[TIMEOUTSECONDS_PARAMETER_NAME]?.toLongOrNull() ?: TIMEOUTSECONDS_PARAMETER_DEFAULT
        val pollingIntervalMs = field.parameters[POLLINGINTERVALMS_PARAMETER_NAME]?.toLongOrNull() ?: POLLINGINTERVALMS_PARAMETER_DEFAULT
        val retries = field.parameters[RETRIES_PARAMETER_NAME]?.toIntOrNull() ?: RETRIES_PARAMETER_DEFAULT
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Generate vector for content element. */
        val vector = when (val c = content.first { it is ImageContent || it is TextContent }) {
            is ImageContent -> Value.FloatVector(ApiWrapper(host, this.model, timeoutSeconds, pollingIntervalMs, retries).imageEmbedding(c.content).toFloatArray())
            is TextContent -> Value.FloatVector(ApiWrapper(host, this.model, timeoutSeconds, pollingIntervalMs, retries).textEmbedding(c.content).toFloatArray())
            else -> throw IllegalArgumentException("Unsupported content type ${c.javaClass.simpleName}.")
        }

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = vector, k = k, fetchVector = fetchVector), context)
    }

    /**
     * Performs analysis on the provided [Retrievable] using the given [ApiWrapper].
     *
     * @param retrievables [Retrievable] to analyse.
     * @param api [ApiWrapper] to use for analysis.
     * @param field The [Schema.Field] to perform the analysis for.
     * @param parameters Additional parameters for the analysis.
     */
    @Suppress("UNCHECKED_CAST")
    override fun analyse(retrievables: Retrievable, api: ApiWrapper, field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>?, parameters: Map<String, String>): List<FloatVectorDescriptor> {
        val imageContent = retrievables.findContent { it is ImageContent } as List<ImageContent>
        val textContent = retrievables.findContent { it is TextContent } as List<TextContent>

        /* Perform image analysis. */
        val d1 = if (imageContent.isNotEmpty()) {
            val imageResult = api.imageEmbedding(imageContent.map { it.content })
            imageResult.map { FloatVectorDescriptor(UUID.randomUUID(), retrievables.id, Value.FloatVector(it.toFloatArray()), field) }
        } else {
            emptyList()
        }

        /* Perform text analysis. */
        val d2 = if (textContent.isNotEmpty()) {
            val textResults = api.textEmbedding(textContent.map { it.content })
            textResults.map { FloatVectorDescriptor(UUID.randomUUID(), retrievables.id, Value.FloatVector(it.toFloatArray()), field) }
        } else {
            emptyList()
        }
        return d1 + d2
    }
}