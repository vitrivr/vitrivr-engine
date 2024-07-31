package org.vitrivr.engine.base.features.external.implementations

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
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
import java.util.*


private val logger: KLogger = KotlinLogging.logger {}

/**
 * Implementation of the [ImageCaption] [ExternalFesAnalyser] that uses the [ApiWrapper] to extract captions from images.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class ImageCaption : ExternalFesAnalyser<ContentElement<*>, StringDescriptor>() {
    companion object {
        const val PROMPT_PARAMETER_NAME = "prompt"
    }

    override val defaultModel = "blip2"

    /**
     * Analyse the provided [ImageContent] using the texts as prompts and the provided [apiWrapper] and return a list of [StringDescriptor]s.
     * If the textContent parameter is set, the prompt is used for conditional captioning.
     *
     * @param content List of [ImageContent] to analyse.
     * @param apiWrapper [ApiWrapper] to use for the analysis.
     */
    fun makeCaption(imageContent: List<ImageContent>, text: List<String?>, apiWrapper: ApiWrapper): List<StringDescriptor> {
        val withTextIndices = text.mapIndexedNotNull { index, t -> if (t != null) index to t else null }
        val withoutTextIndices = text.mapIndexedNotNull { index, t -> if (t == null) index else null }

        val withTextResults = if (withTextIndices.isNotEmpty()) {
            val imageContentsWithText = withTextIndices.map { imageContent[it.first].content }
            val textsWithText = withTextIndices.map { it.second }
            val results = apiWrapper.conditionalImageCaptioning(imageContentsWithText, textsWithText)
            withTextIndices.mapIndexed { index, pair -> pair.first to results[index] }
        } else {
            emptyList()
        }

        val withoutTextResults = if (withoutTextIndices.isNotEmpty()) {
            val imageContentsWithoutText = withoutTextIndices.map { imageContent[it].content }
            val results = apiWrapper.imageCaptioning(imageContentsWithoutText)
            withoutTextIndices.mapIndexed { index, i -> i to results[index] }
        } else {
            emptyList()
        }

        val mergedResults = (withTextResults + withoutTextResults).sortedBy { it.first }
        return mergedResults.map { StringDescriptor(UUID.randomUUID(), null, Value.String(it.second)) }
    }

    /**
     * Analyse the provided [content] using the provided [apiWrapper] and return a list of [StringDescriptor]s.
     *
     * @param content Nested list of [ContentElement] to analyse.
     * @param apiWrapper [ApiWrapper] to use for the analysis.
     * @param parameters Parameters to use for the analysis.
     */
    override fun analyse(
        content: List<List<ContentElement<*>>>,
        apiWrapper: ApiWrapper,
        parameters: Map<String, String>
    ): List<List<StringDescriptor>> {
        val promptDefault = parameters[PROMPT_PARAMETER_NAME]

        val imageContents = content.map { it.filterIsInstance<ImageContent>() }

        val texts = content.map { it.filterIsInstance<TextContent>().map { it.content } }.mapIndexed {
                index, text -> if (text.isEmpty()) {
            List(imageContents[index].size) { promptDefault }
        } else {
            if (text.size != 1) {
                logger.warn { "Text content has more than one element. Only the first element will be used as an image captioning prompt." }
            }
            List(imageContents[index].size) { text.first() }
        }
        }

        val flatResults = makeCaption(imageContents.flatten(), texts.flatten(), apiWrapper)
        var index = 0
        return imageContents.map { innerList ->
            innerList.map { _ ->
                flatResults[index++]
            }
        }

    }

    override val contentClasses = setOf(ImageContent::class, TextContent::class)
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
        field: Schema.Field<ContentElement<*>, StringDescriptor>,
        input: Operator<Retrievable>,
        context: IndexContext
    ): Extractor<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        val batchSize = context.getProperty(field.fieldName, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        val contentSources = context.getProperty(field.fieldName, "contentSources")?.split(",")?.toSet()
        return object : FesExtractor<StringDescriptor, ContentElement<*>, ImageCaption>(input, field, batchSize, contentSources) {
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
    ): Extractor<ContentElement<*>, StringDescriptor> {
        val batchSize = context.getProperty(name, BATCHSIZE_PARAMETER_NAME)?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        val contentSources = context.getProperty(name, "contentSources")?.split(",")?.toSet()
        return object : FesExtractor<StringDescriptor, ContentElement<*>, ImageCaption>(input, null, batchSize, contentSources) {
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
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, StringDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
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
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, StringDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L

        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = Value.String(text.content), limit = limit), context)
    }


}
