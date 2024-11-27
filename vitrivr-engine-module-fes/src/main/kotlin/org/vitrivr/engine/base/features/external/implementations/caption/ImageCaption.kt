package org.vitrivr.engine.base.features.external.implementations.caption

import org.vitrivr.engine.base.features.external.api.AbstractApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextPredicate
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*


/**
 * Implementation of the [ImageCaption] [ExternalFesAnalyser] that uses the [AbstractApi] to extract captions from images.
 *
 * @author Fynn Faber
 * @version 1.1.0
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
    override fun prototype(field: Schema.Field<*, *>): TextDescriptor = TextDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Text(""))

    /**
     * Generates and returns a new [ImageCaptionExtractor] instance for this [ImageCaption].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [ImageCaptionExtractor].
     * @param context The [IndexContext] to use with the [ImageCaptionExtractor].
     * @return [ImageCaptionExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = TODO("type mismatch") // = ImageCaptionExtractor(input, name, this, emptyMap())

    /**
     * Generates and returns a new [ImageCaptionExtractor] instance for this [ImageCaption].
     *
     * @param field The [Schema.Field] to create an [ImageCaptionExtractor] for.
     * @param input The [Operator] that acts as input to the new [ImageCaptionExtractor].
     * @param context The [IndexContext] to use with the [ImageCaptionExtractor].
     * @return [ImageCaptionExtractor]
     */
    override fun newExtractor(field: Schema.Field<ContentElement<*>, TextDescriptor>, input: Operator<Retrievable>, context: IndexContext) = TODO("type mismatch") //ImageCaptionExtractor(input, field, this, merge(field, context) )

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ExternalFesAnalyser].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ExternalFesAnalyser]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, TextDescriptor>, query: Query, context: QueryContext) = FulltextRetriever(field, query, context)

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ExternalFesAnalyser].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, TextDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, TextDescriptor> {
        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val predicate = SimpleFulltextPredicate(field = field, value = Value.Text(text.content))
        return this.newRetrieverForQuery(field, Query(predicate, limit), context)
    }
}