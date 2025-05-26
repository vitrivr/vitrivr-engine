package org.vitrivr.engine.base.features.external.implementations.caption

import org.vitrivr.engine.base.features.external.api.AbstractApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Analyser.Companion.merge
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*


/**
 * Implementation of the [ImageCaption] [ExternalFesAnalyser] that uses the [AbstractApi] to extract captions from images.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class ImageCaption : ExternalFesAnalyser<ContentElement<*>, TextDescriptor>() {
    companion object {
        const val PROMPT_PARAMETER_NAME = "prompt"
    }

    override val contentClasses = setOf(ImageContent::class, TextContent::class)
    override val descriptorClass = TextDescriptor::class

    /**
     * Generates a prototypical [TextDescriptor] for this [ExternalFesAnalyser].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [TextDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): TextDescriptor =
        TextDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Text(""))

    /**
     * Generates and returns a new [ImageCaptionExtractor] instance for this [ImageCaption].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [ImageCaptionExtractor].
     * @param context The [Context] to use with the [ImageCaptionExtractor].
     * @return [ImageCaptionExtractor]
     */
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        parameters: Map<String, String>,
        context: Context
    ) = TODO("type mismatch") // = ImageCaptionExtractor(input, name, this, emptyMap())

    /**
     * Generates and returns a new [ImageCaptionExtractor] instance for this [ImageCaption].
     *
     * @param field The [Schema.Field] to create an [ImageCaptionExtractor] for.
     * @param input The [Operator] that acts as input to the new [ImageCaptionExtractor].
     * @param context The [Context] to use with the [ImageCaptionExtractor].
     * @return [ImageCaptionExtractor]
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        input: Operator<Retrievable>,
        parameters: Map<String, String>,
        context: Context
    ) = TODO("type mismatch") //ImageCaptionExtractor(input, field, this, merge(field, context) )

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ExternalFesAnalyser].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [Context] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ExternalFesAnalyser]
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        query: Query,
        context: Context
    ): Retriever<ContentElement<*>, TextDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ExternalFesAnalyser].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [Context] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        content: Collection<ContentElement<*>>,
        context: Context
    ): Retriever<ContentElement<*>, TextDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull()
            ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        return this.newRetrieverForQuery(
            field,
            SimpleFulltextQuery(value = Value.Text(text.content), limit = limit),
            context
        )
    }
}