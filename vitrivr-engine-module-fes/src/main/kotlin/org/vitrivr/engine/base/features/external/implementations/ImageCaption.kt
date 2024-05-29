package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.module.features.feature.fulltext.FulltextRetriever
import java.util.*


/**
 * Implementation of the [ImageCaption] [ExternalFesAnalyser] that uses the [ApiWrapper] to extract captions from images.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class ImageCaption : ExternalFesAnalyser<ImageContent, StringDescriptor>() {
    companion object {
        const val PROMPT_PARAMETER_NAME = "prompt"
    }

    override val defaultModel = "blip2"

    /**
    * Analyse the provided [ImageContent] using the provided [apiWrapper] and return a list of [StringDescriptor]s.
    * If the prompt parameter is set, the prompt is used for conditional captioning.
    *
    * @param content List of [ImageContent] to analyse.
    * @param apiWrapper [ApiWrapper] to use for the analysis.
     */
    override fun analyseFlattened(content: List<ImageContent>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<StringDescriptor>> {
        val prompt = parameters[PROMPT_PARAMETER_NAME]
        if (prompt != null) {
            val result = apiWrapper.conditionalImageCaptioning(content.map { it.content }, List(content.size) { prompt })
            return result.map { listOf(StringDescriptor(UUID.randomUUID(), null, Value.String(it))) }
        }
        val result = apiWrapper.imageCaptioning(content.map { it.content })
        return result.map { listOf(StringDescriptor(UUID.randomUUID(), null, Value.String(it))) }
    }

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = StringDescriptor::class

    /**
     * Generates a prototypical [StringDescriptor] for this [ExternalFesAnalyser].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [StringDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): StringDescriptor = StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""))

    /**
     * Generates and returns a new [FesExtractor] instance for this [ExternalFesAnalyser].
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor].
     * @param context The [IndexContext] to use with the [Extractor].
     *
     * @return A new [Extractor] instance for this [ExternalFesAnalyser]
     * @throws [UnsupportedOperationException], if this [ExternalFesAnalyser] does not support the creation of an [Extractor] instance.
     */
    override fun newExtractor(
        field: Schema.Field<ImageContent, StringDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        val batchSize = context.getProperty(field.fieldName, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        return object : FesExtractor<StringDescriptor, ImageContent, ImageCaption>(input, field, batchSize) {
            override fun assignRetrievableId(descriptor: StringDescriptor, retrievableId: RetrievableId): StringDescriptor {
                return descriptor.copy(retrievableId = retrievableId, field = field)
            }
        }
    }

    /**
     * Generates and returns a new [FesExtractor] instance for this [ExternalFesAnalyser].
     *
     * @param name The name of the [Extractor].
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     *
     * @return A new [FesExtractor] instance for this [ExternalFesAnalyser]
     */
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ImageContent, StringDescriptor> {
        val batchSize = context.getProperty(name, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        return object : FesExtractor<StringDescriptor, ImageContent, ImageCaption>(input, null, batchSize) {
            override fun assignRetrievableId(descriptor: StringDescriptor, retrievableId: RetrievableId): StringDescriptor {
                return descriptor.copy(retrievableId = retrievableId)
            }
        }
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ExternalFesAnalyser].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ExternalFesAnalyser]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, StringDescriptor>, query: Query, context: QueryContext): Retriever<ImageContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ExternalFesAnalyser].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ImageContent] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, StringDescriptor>, content: Collection<ImageContent>, context: QueryContext): Retriever<ImageContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L

        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = Value.String(text.content), limit = limit), context)
    }


}