package org.vitrivr.engine.module.features.feature.external.implementations.prak

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.UUID

/** Retrieval-only video QA. No descriptors need to be ingested for this field. */
class VideoQA : Analyser<ContentElement<*>, TextDescriptor> {
    override val contentClasses = setOf(TextContent::class)
    override val descriptorClass = TextDescriptor::class
    override fun prototype(field: Schema.Field<*, *>) = TextDescriptor(UUID.randomUUID(), null, Value.Text(""))

    override fun newExtractor(field: Schema.Field<ContentElement<*>, TextDescriptor>, input: Operator<out Retrievable>, context: Context): Extractor<ContentElement<*>, TextDescriptor> =
        throw UnsupportedOperationException("VideoQA supports retrieval only.")

    override fun newExtractor(name: String, input: Operator<out Retrievable>, context: Context): Extractor<ContentElement<*>, TextDescriptor> =
        throw UnsupportedOperationException("VideoQA supports retrieval only.")

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, TextDescriptor>, query: Query, context: Context): VideoQARetriever {
        require(query is SimpleBooleanQuery<*>) { "VideoQA requires a textual SimpleBooleanQuery." }
        val question = when (val value = query.value) {
            is Value.Text -> value.value
            is Value.String -> value.value
            else -> throw IllegalArgumentException("VideoQA requires a textual question.")
        }
        return VideoQARetriever(field, question, context)
    }

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, TextDescriptor>, content: Map<String, ContentElement<*>>, context: Context): VideoQARetriever {
        val texts = content.values.filterIsInstance<TextContent>()
        require(texts.size == 1 && content.size == 1) { "VideoQA requires exactly one text input." }
        return VideoQARetriever(field, texts.single().text, context)
    }

    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, TextDescriptor>, descriptors: Collection<TextDescriptor>, context: Context): VideoQARetriever {
        require(descriptors.size == 1) { "VideoQA requires exactly one question descriptor." }
        return VideoQARetriever(field, descriptors.single().value.value, context)
    }
}
