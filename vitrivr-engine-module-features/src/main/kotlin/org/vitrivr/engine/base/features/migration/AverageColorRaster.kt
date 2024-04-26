package org.vitrivr.engine.base.features.migration

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.struct.RasterDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class AverageColorRaster: Analyser<ContentElement<*>, RasterDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = RasterDescriptor::class
    override fun prototype(field: Schema.Field<*, *>): RasterDescriptor =
        RasterDescriptor(id = UUID.randomUUID(), retrievableId = UUID.randomUUID(), hist = List(15) { Value.Float(0.0f) }, raster = List(64) { Value.Float(0.0f) }) // should transient be false? what is transient?

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, RasterDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, RasterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, RasterDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, RasterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, RasterDescriptor>?, input: Operator<Ingested>, context: IndexContext): Extractor<ContentElement<*>, RasterDescriptor> {
        TODO("Not yet implemented")
    }
}