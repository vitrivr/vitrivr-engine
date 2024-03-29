package org.vitrivr.engine.core.features.migration

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.metadata.MediaDimensionsDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class  MediaDimensions : Analyser<ContentElement<*>, MediaDimensionsDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = MediaDimensionsDescriptor::class
    override fun prototype(field: Schema.Field<*,*>) = MediaDimensionsDescriptor(UUID.randomUUID(), UUID.randomUUID(), 0, 0, true)


    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, MediaDimensionsDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, MediaDimensionsDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForDescriptors(field: Schema.Field<ContentElement<*>, MediaDimensionsDescriptor>, descriptors: Collection<MediaDimensionsDescriptor>, context: QueryContext): Retriever<ContentElement<*>, MediaDimensionsDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, MediaDimensionsDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, MediaDimensionsDescriptor> {
        TODO("Not yet implemented")
    }

}