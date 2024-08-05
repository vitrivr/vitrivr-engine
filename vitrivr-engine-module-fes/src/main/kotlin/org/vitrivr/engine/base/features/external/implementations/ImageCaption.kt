package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.retrieve.Retriever
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

    override val model = "blip2"

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

    /**
     * Performs analysis on the provided [Retrievable] using the given [ApiWrapper].
     *
     * @param retrievables [Retrievable] to analyse.
     * @param api [ApiWrapper] to use for analysis.
     * @param field The [Schema.Field] to perform the analysis for.
     * @param parameters Additional parameters for the analysis.
     */
    @Suppress("UNCHECKED_CAST")
    override fun analyse(retrievables: Retrievable, api: ApiWrapper, field: Schema.Field<ImageContent, StringDescriptor>?, parameters: Map<String, String>): List<StringDescriptor> {
        val prompt = parameters[PROMPT_PARAMETER_NAME]
        val content = retrievables.findContent { it is ImageContent } as List<ImageContent>
        if (content.isEmpty()) return emptyList()
        if (prompt != null) {
            val result = api.conditionalImageCaptioning(content.map { it.content }, List(content.size) { prompt })
            return result.map { StringDescriptor(UUID.randomUUID(), retrievables.id, Value.String(it), field) }
        } else {
            val result = api.imageCaptioning(content.map { it.content })
            return result.map { StringDescriptor(UUID.randomUUID(), retrievables.id, Value.String(it), field) }
        }
    }
}