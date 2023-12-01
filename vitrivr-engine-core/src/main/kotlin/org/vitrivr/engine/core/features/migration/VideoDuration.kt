package org.vitrivr.engine.core.features.migration

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.FloatDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class VideoDuration : Analyser<ContentElement<*>, FloatDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = FloatDescriptor::class
    override fun prototype() = FloatDescriptor(UUID.randomUUID(), UUID.randomUUID(),transient = true, value=.0f)
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, FloatDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, FloatDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, FloatDescriptor>, descriptors: Collection<FloatDescriptor>, context: QueryContext): Retriever<ContentElement<*>, FloatDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, FloatDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, FloatDescriptor> {
        TODO("Not yet implemented")
    }


}