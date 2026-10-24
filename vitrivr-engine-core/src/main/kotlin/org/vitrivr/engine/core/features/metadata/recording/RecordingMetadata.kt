package org.vitrivr.engine.core.features.metadata.recording

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.RecordingMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever

/** Derives day and recording source from paths without decoding media. */
class RecordingMetadata : Analyser<ContentElement<*>, RecordingMetadataDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = RecordingMetadataDescriptor::class

    override fun prototype(field: Schema.Field<*, *>) = RecordingMetadataDescriptor.PROTOTYPE

    override fun newExtractor(field: Schema.Field<ContentElement<*>, RecordingMetadataDescriptor>, input: Operator<out Retrievable>, context: Context) = RecordingMetadataExtractor(input, this, field, context)

    override fun newExtractor(name: String, input: Operator<out Retrievable>, context: Context) = RecordingMetadataExtractor(input, this, name, context)

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, RecordingMetadataDescriptor>, query: Query, context: Context): Retriever<ContentElement<*>, RecordingMetadataDescriptor> {
        require(field.analyser == this) { "Field type is incompatible with analyser. This is a programmer's error!" }
        require(query is SimpleBooleanQuery<*>) { "Recording metadata supports simple boolean queries." }
        return org.vitrivr.engine.core.features.bool.StructBooleanRetriever(field, query, context)
    }

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, RecordingMetadataDescriptor>, content: Map<String, ContentElement<*>>, context: Context): Retriever<ContentElement<*>, RecordingMetadataDescriptor> {
        throw UnsupportedOperationException("RecordingMetadata does not support the creation of a Retriever instance from content.")
    }

}