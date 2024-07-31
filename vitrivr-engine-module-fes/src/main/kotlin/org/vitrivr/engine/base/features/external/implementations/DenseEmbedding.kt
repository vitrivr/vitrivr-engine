package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.context.IndexContext
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
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * Implementation of the [DenseEmbedding] [ExternalFesAnalyser] that uses the [ApiWrapper] to analyse image and text content.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class DenseEmbedding : ExternalFesAnalyser<ContentElement<*>, FloatVectorDescriptor>() {
companion object {
        const val LENGTH_PARAMETER_DEFAULT = 512
        const val LENGTH_PARAMETER_NAME = "length"
    }

    override val defaultModel = "clip-vit-large-patch14"

    /**
     * Analyse the provided [ContentElement]s using the provided [apiWrapper] and return a list of [FloatVectorDescriptor]s.
     *
     * @param content List of [ContentElement] to analyse.
     * @param apiWrapper [ApiWrapper] to use for the analysis.
     * @param parameters Map of parameters to use for the analysis.
     * @return List of [FloatVectorDescriptor]s.
     */
    override fun analyseFlattened(content: List<ContentElement<*>>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<FloatVectorDescriptor>> {

        val imageContents = content.filterIsInstance<ImageContent>()
        val textContents = content.filterIsInstance<TextContent>()

        var imageResults: List<List<Float>>? = null
        var textResults: List<List<Float>>? = null


        if (imageContents.isNotEmpty()) {
            imageResults = apiWrapper.imageEmbedding(imageContents.map { it.content })
        }
        if (textContents.isNotEmpty()) {
            if ("retrievalTaskInstructions" in parameters) {
                textResults = apiWrapper.textQueryEmbedding(textContents.map { it.content }, parameters["retrievalTaskInstructions"]!!)
            }else{
                textResults = apiWrapper.textEmbedding(textContents.map { it.content })
            }
        }

        return content.map { element ->
            when (element) {
                is ImageContent -> {
                    val index = imageContents.indexOf(element)
                    listOf(FloatVectorDescriptor(UUID.randomUUID(), null, Value.FloatVector(imageResults!![index].toFloatArray()), null))
                }
                is TextContent -> {
                    val index = textContents.indexOf(element)
                    listOf(FloatVectorDescriptor(UUID.randomUUID(), null, Value.FloatVector(textResults!![index].toFloatArray()), null))
                }
                else -> throw(IllegalArgumentException("Content type not supported"))
            }
        }
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

        val parameters = field.parameters.toMutableMap()

        /* Prepare query parameters. */
        if (context.getProperty(field.fieldName, "retrievalTaskInstructions") != null) {
            parameters["retrievalTaskInstructions"] = context.getProperty(field.fieldName, "retrievalTaskInstructions")!!
        }
        val vector = analyse(content.first(), parameters)
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(field, ProximityQuery(value = vector.vector, k = k, fetchVector = fetchVector), context)
    }

    /**
     * Generates and returns a new [Extractor] instance for this [DenseEmbedding].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] to use with the [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     * @return [FloatVectorDescriptor]
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): FesExtractor<FloatVectorDescriptor, ContentElement<*>, DenseEmbedding> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        val batchSize = context.getProperty(field.fieldName, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        val contentSources = context.getProperty(field.fieldName, "contentSources")?.split(",")?.toSet()
        return object : FesExtractor<FloatVectorDescriptor, ContentElement<*>, DenseEmbedding>(input, field, batchSize, contentSources) {
            override fun assignRetrievableId(descriptor: FloatVectorDescriptor, retrievableId: RetrievableId): FloatVectorDescriptor {
                return descriptor.copy(retrievableId = retrievableId, field = field)
            }
        }
    }

    /**
     * Generates and returns a new [Extractor] instance for this [DenseEmbedding].
     *
     * @param name The name of the [Extractor].
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     */
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): FesExtractor<FloatVectorDescriptor, ContentElement<*>, DenseEmbedding>{
        val batchSize = context.getProperty(name, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        val contentSources = context.getProperty(name, "contentSources")?.split(",")?.toSet()
        return object : FesExtractor<FloatVectorDescriptor, ContentElement<*>, DenseEmbedding>(input, null, batchSize, contentSources) {
            override fun assignRetrievableId(descriptor: FloatVectorDescriptor, retrievableId: RetrievableId): FloatVectorDescriptor {
                return descriptor.copy(retrievableId = retrievableId)
            }
        }
    }


}