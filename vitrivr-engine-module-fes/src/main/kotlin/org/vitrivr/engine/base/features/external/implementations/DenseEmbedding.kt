package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.DenseRetriever
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
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
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class DenseEmbedding : ExternalFesAnalyser<ContentElement<*>, FloatVectorDescriptor>() {
companion object {
        const val LENGTH_PARAMETER_DEFAULT = 512
        const val LENGTH_PARAMETER_NAME = "length"
    }

    override val defaultModel = "clip-vit-large-patch14"
    override fun analyseFlattened(content: List<ContentElement<*>>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<FloatVectorDescriptor>> {

        val imageContents = content.filterIsInstance<ImageContent>()
        val textContents = content.filterIsInstance<TextContent>()

        var imageResults: List<List<Float>>? = null
        var textResults: List<List<Float>>? = null


        if (imageContents.isNotEmpty()) {
            imageResults = apiWrapper.imageEmbedding(imageContents.map { it.content })
        }
        if (textContents.isNotEmpty()) {
            textResults = apiWrapper.textEmbedding(textContents.map { it.content })
        }

        return content.map { element ->
            when (element) {
                is ImageContent -> {
                    val index = imageContents.indexOf(element)
                    listOf(FloatVectorDescriptor(UUID.randomUUID(), null, imageResults!![index].map { Value.Float(it) }, null))
                }
                is TextContent -> {
                    val index = textContents.indexOf(element)
                    listOf(FloatVectorDescriptor(UUID.randomUUID(), null, textResults!![index].map { Value.Float(it) }, null))
                }
                else -> throw(IllegalArgumentException("Content type not supported"))
            }
        }
    }

    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = FloatVectorDescriptor::class

    override fun prototype(field: Schema.Field<*, *>) : FloatVectorDescriptor {
        //convert to integer
        val length = field.parameters[LENGTH_PARAMETER_NAME]?.toIntOrNull() ?: LENGTH_PARAMETER_DEFAULT
        return FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), List(length) { Value.Float(0.0f) })
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is ProximityQuery<*> && query.value.first() is Value.Float) { "The query is not a ProximityQuery<Value.Float>." }
        @Suppress("UNCHECKED_CAST")
        return DenseRetriever(field, query as ProximityQuery<Value.Float>, context)
    }

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {

        /* Prepare query parameters. */
        val vector = analyse(content.first(), field.parameters)
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = vector.vector, k = k, fetchVector = fetchVector), context)
    }

    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, FloatVectorDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        val batchSize = context.getProperty(field.fieldName, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        return object : FesExtractor<FloatVectorDescriptor, ContentElement<*>, DenseEmbedding>(input, field, batchSize) {
            override fun assignRetrievableId(descriptor: FloatVectorDescriptor, retrievableId: RetrievableId): FloatVectorDescriptor {
                return descriptor.copy(retrievableId = retrievableId, field = field)
            }
        }
    }

    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, FloatVectorDescriptor> {
        val batchSize = context.getProperty(name, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        return object : FesExtractor<FloatVectorDescriptor, ContentElement<*>, DenseEmbedding>(input, null, batchSize) {
            override fun assignRetrievableId(descriptor: FloatVectorDescriptor, retrievableId: RetrievableId): FloatVectorDescriptor {
                return descriptor.copy(retrievableId = retrievableId)
            }
        }
    }


}