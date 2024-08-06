package org.vitrivr.engine.base.features.external.implementations.asr

import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*
import org.vitrivr.engine.core.model.types.Value.String as StringValue

/**
 * [ExternalFesAnalyser] for the Automated Speech Recognition (ASR).
 *
 * @author Ralph Gasser
 * @author Fynn Faber
 * @version 1.1.0
 */
class ASR : ExternalFesAnalyser<AudioContent, StringDescriptor>() {
    override val contentClasses = setOf(AudioContent::class)
    override val descriptorClass = StringDescriptor::class

    /**
     * Generates a prototypical [StringDescriptor] for this [ASR].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [StringDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): StringDescriptor {
        return StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), StringValue(""))
    }

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [ASRExtractor].
     * @param context The [IndexContext] to use with the [ASRExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = ASRExtractor(input, null, this, context.local[name] ?: emptyMap())

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [ASRExtractor] for.
     * @param input The [Operator] that acts as input to the new [ASRExtractor].
     * @param context The [IndexContext] to use with the [ASRExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(field: Schema.Field<AudioContent, StringDescriptor>, input: Operator<Retrievable>, context: IndexContext) = ASRExtractor(input, field, this, field.parameters)

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ExternalFesAnalyser]
     */
    override fun newRetrieverForQuery(field: Schema.Field<AudioContent, StringDescriptor>, query: Query, context: QueryContext): Retriever<AudioContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [StringDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<AudioContent, StringDescriptor>, descriptors: Collection<StringDescriptor>, context: QueryContext): Retriever<AudioContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters and return retriever. */
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = descriptors.first().value, limit = limit), context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<AudioContent, StringDescriptor>, content: Collection<AudioContent>, context: QueryContext): Retriever<AudioContent, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters and return retriever. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = Value.String(text.content), limit = limit), context)
    }
}