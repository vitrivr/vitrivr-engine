package org.vitrivr.engine.base.features.external.implementations.ocr

import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.implementations.asr.ASR
import org.vitrivr.engine.base.features.external.implementations.asr.ASRExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser.Companion.merge
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * [ExternalFesAnalyser] for the Optical Chracter Recognition (OCR).
 *
 * @author Ralph Gasser
 * @author Fynn Faber
 * @version 1.1.0
 */
class OCR : ExternalFesAnalyser<ImageContent, TextDescriptor>() {
    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = TextDescriptor::class

    /**
     * Generates a prototypical [TextDescriptor] for this [OCR].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [TextDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): TextDescriptor =
        TextDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Text(""))

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(
        name: String,
        input: Operator<Retrievable>,
        parameters: Map<String, String>,
        context: IndexContext
    ) = OCRExtractor(input, name, this, parameters)

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [FesExtractor] for.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(
        field: Schema.Field<ImageContent, TextDescriptor>,
        input: Operator<Retrievable>,
        parameters: Map<String, String>,
        context: IndexContext
    ) = OCRExtractor(input, field, this, merge(field, parameters))

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [OCR]
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<ImageContent, TextDescriptor>,
        query: Query,
        context: QueryContext
    ): FulltextRetriever<ImageContent> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [TextDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ImageContent, TextDescriptor>,
        descriptors: Collection<TextDescriptor>,
        context: QueryContext
    ): Retriever<ImageContent, TextDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters and return retriever. */
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        return this.newRetrieverForQuery(
            field,
            SimpleFulltextQuery(value = descriptors.first().value, limit = limit),
            context
        )
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ImageContent, TextDescriptor>,
        content: Collection<ImageContent>,
        context: QueryContext
    ): FulltextRetriever<ImageContent> {
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