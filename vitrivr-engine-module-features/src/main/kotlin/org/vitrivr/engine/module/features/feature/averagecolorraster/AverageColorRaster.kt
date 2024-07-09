package org.vitrivr.engine.module.features.feature.averagecolorraster

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class AverageColorRaster: Analyser<ContentElement<*>, RasterDescriptor> {
    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = RasterDescriptor::class
    override fun prototype(field: Schema.Field<*, *>): RasterDescriptor =
        RasterDescriptor(id = UUID.randomUUID(), retrievableId = UUID.randomUUID(), mapOf("hist" to Value.FloatVector(FloatArray(15)), "raster" to Value.FloatVector(FloatArray(64))))

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, RasterDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, RasterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, RasterDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, RasterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, RasterDescriptor>, input: Operator<Retrievable>, context: IndexContext): Extractor<ContentElement<*>, RasterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): Extractor<ContentElement<*>, RasterDescriptor> {
        TODO("Not yet implemented")
    }
}