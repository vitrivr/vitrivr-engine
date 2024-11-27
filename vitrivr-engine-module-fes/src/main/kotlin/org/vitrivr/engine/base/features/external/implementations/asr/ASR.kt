package org.vitrivr.engine.base.features.external.implementations.asr

import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser.Companion.merge
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextPredicate
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

/**
 * [ExternalFesAnalyser] for the Automated Speech Recognition (ASR).
 *
 * @author Ralph Gasser
 * @author Fynn Faber
 * @version 1.2.0
 */
class ASR : ExternalFesAnalyser<AudioContent, TextDescriptor>() {
    override val contentClasses = setOf(AudioContent::class)
    override val descriptorClass = TextDescriptor::class

    /**
     * Generates a prototypical [TextDescriptor] for this [ASR].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [TextDescriptor]
     */
    override fun prototype(field: Schema.Field<*, *>): TextDescriptor {
        return TextDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Text(""))
    }

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [ASRExtractor].
     * @param context The [IndexContext] to use with the [ASRExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = ASRExtractor(input, name, this, context.local[name] ?: emptyMap())

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [ASRExtractor] for.
     * @param input The [Operator] that acts as input to the new [ASRExtractor].
     * @param context The [IndexContext] to use with the [ASRExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(field: Schema.Field<AudioContent, TextDescriptor>, input: Operator<Retrievable>, context: IndexContext) = ASRExtractor(input, field, this, merge(field, context))

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ExternalFesAnalyser]
     */
    override fun newRetrieverForQuery(field: Schema.Field<AudioContent, TextDescriptor>, query: Query, context: QueryContext) = FulltextRetriever(field, query, context)

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [TextDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<AudioContent, TextDescriptor>, descriptors: Collection<TextDescriptor>, context: QueryContext): Retriever<AudioContent, TextDescriptor> {
        /* Prepare query parameters and return retriever. */
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val predicate = SimpleFulltextPredicate(field = field, value = descriptors.first().value)
        return this.newRetrieverForQuery(field, Query(predicate, limit), context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<AudioContent, TextDescriptor>, content: Collection<AudioContent>, context: QueryContext): Retriever<AudioContent, TextDescriptor> {
        /* Prepare query parameters and return retriever. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val predicate = SimpleFulltextPredicate(field = field, value = Value.Text(text.content))
        return this.newRetrieverForQuery(field, Query(predicate, limit), context)
    }
}