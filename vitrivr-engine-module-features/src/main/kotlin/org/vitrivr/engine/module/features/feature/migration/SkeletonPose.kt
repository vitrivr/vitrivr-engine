package org.vitrivr.engine.module.features.feature.migration

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.SkeletonDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever

class SkeletonPose : Analyser<ContentElement<*>, SkeletonDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = SkeletonDescriptor::class
    override fun prototype(field: Schema.Field<*, *>): SkeletonDescriptor = SkeletonDescriptor(
        id = java.util.UUID.randomUUID(),
        retrievableId = java.util.UUID.randomUUID(),
        person = Value.Int(0),
        skeleton = List(12) { Value.Float(0.0f) },
        weights = List(12) { Value.Float(0.0f) }
    ) // should transient be false? what is transient?

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, SkeletonDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, SkeletonDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, SkeletonDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, SkeletonDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, SkeletonDescriptor>, input: Operator<Retrievable>, context: IndexContext): Extractor<ContentElement<*>, SkeletonDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<ContentElement<*>, SkeletonDescriptor> {
        TODO("Not yet implemented")
    }
}