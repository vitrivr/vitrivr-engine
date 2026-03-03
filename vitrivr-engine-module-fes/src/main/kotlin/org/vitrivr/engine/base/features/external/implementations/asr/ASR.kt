package org.vitrivr.engine.base.features.external.implementations.asr

import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
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
 * @version 1.1.0
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
     * @param context The [Context] to use with the [ASRExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(
        name: String,
        input: Operator<out Retrievable>,
        context: Context
    ) = ASRExtractor(input, name, this, context)

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [ASRExtractor] for.
     * @param input The [Operator] that acts as input to the new [ASRExtractor].
     * @param context The [Context] to use with the [ASRExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(
        field: Schema.Field<AudioContent, TextDescriptor>,
        input: Operator<out Retrievable>,
        context: Context
    ) = ASRExtractor(input, field, this, context)

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [Context] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ExternalFesAnalyser]
     */
    override fun newRetrieverForQuery(
        field: Schema.Field<AudioContent, TextDescriptor>,
        query: Query,
        context: Context
    ): Retriever<AudioContent, TextDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [TextDescriptor] elements to use with the [Retriever]
     * @param context The [Context] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<AudioContent, TextDescriptor>,
        descriptors: Collection<TextDescriptor>,
        context: Context
    ): Retriever<AudioContent, TextDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters and return retriever. */
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        return this.newRetrieverForQuery(
            field,
            SimpleFulltextQuery(value = descriptors.first().value, limit = limit),
            context
        )
    }

}