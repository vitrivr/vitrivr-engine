package org.vitrivr.engine.base.features.migration

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class CLDReduced15 : Analyser<ContentElement<*>, FloatVectorDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = FloatVectorDescriptor::class
    override fun prototype(field: Schema.Field<*,*>) = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), List(12) { 0.0f }, transient=true)
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, descriptors: Collection<FloatVectorDescriptor>, context: QueryContext): Retriever<ContentElement<*>, FloatVectorDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, FloatVectorDescriptor> {
        TODO("Not yet implemented")
    }
}