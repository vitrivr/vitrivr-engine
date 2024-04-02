package org.vitrivr.engine.base.features.external.implementations

import okhttp3.OkHttpClient
import org.openapitools.client.apis.ImageEmbeddingApi
import org.openapitools.client.apis.TextEmbeddingApi
import org.openapitools.client.models.ImageEmbeddingInput
import org.openapitools.client.models.JobResponseImageEmbeddingOutput
import org.openapitools.client.models.TextEmbeddingInput
import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyzer
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.util.extension.toDataURL
import java.util.*

class DenseEmbedding : ExternalFesAnalyzer<ContentElement<*>, FloatVectorDescriptor>() {
companion object {
        const val LENGTH_PARAMETER_DEFAULT = 512
        const val LENGTH_PARAMETER_NAME = "length"
        const val MODEL_PARAMETER_DEFAULT = "clip-vit-large-patch14"
        const val MODEL_PARAMETER_NAME = "model"
    }


    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    override fun prototype(field: Schema.Field<*, *>) : FloatVectorDescriptor{
        //convert to integer
        val length = field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull() ?: LENGTH_PARAMETER_DEFAULT
        return FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), List(length) { Value.Float(0.0f) }, true)
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is ProximityQuery<*> && query.value.first() is Value.Float) { "The query is not a ProximityQuery<Value.Float>." }
        @Suppress("UNCHECKED_CAST")
        return DenseEmbeddingRetriever(field, query as ProximityQuery<Value.Float>, context)
    }

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        val model = field.parameters[MODEL_PARAMETER_NAME] ?: MODEL_PARAMETER_DEFAULT

        /* Prepare query parameters. */
        val vector = analyse(content.first(), model, host)
        val k = context.getProperty(field.fieldName, "limit")?.toIntOrNull() ?: 1000
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = vector.vector, k = k, fetchVector = fetchVector), context)
    }


    override fun newExtractor(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return DenseEmbeddingExtractor(input, field, persisting)
    }

    override fun analyse(content: ContentElement<*>, apiWrapper: ApiWrapper): FloatVectorDescriptor = when(content) {

        is ImageContent -> {
            val result = apiWrapper.imageEmbedding(content.content) as List<Float>
            FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), result.map { Value.Float(it) }, true)
        }
        is TextContent -> {
            val result = apiWrapper.textEmbedding(content.content) as List<Float>
            FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), result.map { Value.Float(it) }, true)
        }
        else -> throw IllegalArgumentException("Content type not supported")
    }


}