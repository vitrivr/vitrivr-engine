package org.vitrivr.engine.base.features.external.implementations.ocr

import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.implementations.asr.ASR
import org.vitrivr.engine.base.features.external.implementations.asr.ASRExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.ImageContent
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
 * [ExternalFesAnalyser] for the Optical Chracter Recognition (OCR).
 *
 * @author Ralph Gasser
 * @author Fynn Faber
 * @version 1.2.0
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
    override fun prototype(field: Schema.Field<*, *>): TextDescriptor = TextDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Text(""))

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param name The name of the extractor.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext) = OCRExtractor(input, name, this, context.local[name] ?: emptyMap())

    /**
     * Generates and returns a new [ASRExtractor] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [FesExtractor] for.
     * @param input The [Operator] that acts as input to the new [FesExtractor].
     * @param context The [IndexContext] to use with the [FesExtractor].
     * @return [ASRExtractor]
     */
    override fun newExtractor(field: Schema.Field<ImageContent, TextDescriptor>, input: Operator<Retrievable>, context: IndexContext) = OCRExtractor(input, field, this, merge(field, context))

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [OCR]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, TextDescriptor>, query: Query, context: QueryContext) = FulltextRetriever(field, query, context)

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [OCR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [TextDescriptor] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForDescriptors(field: Schema.Field<ImageContent, TextDescriptor>, descriptors: Collection<TextDescriptor>, context: QueryContext): Retriever<ImageContent, TextDescriptor> {
        val limit = context.getProperty(field.fieldName, QueryContext.LIMIT_KEY)?.toLongOrNull() ?: QueryContext.LIMIT_DEFAULT
        val predicate = SimpleFulltextPredicate(field = field, value = descriptors.first().value) /* TODO: More complex predicates? */
        return this.newRetrieverForQuery(field, Query(predicate, limit), context)
    }
}