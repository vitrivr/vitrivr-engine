package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.Content
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
 * Analyser for the Optical Chracter Recognition (OCR).
 *
 * @author Ralph Gasser
 * @author Fynn Faber
 * @version 1.1.0
 */
class OCR : ExternalFesAnalyser<ImageContent, StringDescriptor>() {
    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = StringDescriptor::class
    override val model = "tesseract"

    /**
     * Generates a prototypical [StringDescriptor] for this [OCR].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [StringDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): StringDescriptor = StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""))

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [OCR]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, StringDescriptor>, query: Query, context: QueryContext): FulltextRetriever<ImageContent> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [StringDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, StringDescriptor>, descriptors: Collection<StringDescriptor>, context: QueryContext): Retriever<ImageContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters and return retriever. */
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = descriptors.first().value, limit = limit), context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ImageContent, StringDescriptor>, content: Collection<ImageContent>, context: QueryContext): FulltextRetriever<ImageContent> {
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
        val content = retrievables.findContent { it is ImageContent } as List<ImageContent>
        if (content.isEmpty()) return emptyList()
        val result = api.opticalCharacterRecognition(content.map { it.content })
        return result.map { StringDescriptor(UUID.randomUUID(), retrievables.id, Value.String(it), field) }
    }
}