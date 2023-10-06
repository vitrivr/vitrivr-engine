package org.vitrivr.engine.index.extract

import org.vitrivr.engine.base.features.averagecolor.AverageColorExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.ingest.ExtractorFactory
import org.vitrivr.engine.core.operators.ingest.templates.DummyExtractor

class AverageColorExtractorFactory : ExtractorFactory<ImageContent, FloatVectorDescriptor> {
    override fun newOperator(
        input: Operator<Ingested>,
        parameters: Map<String, Any>,
        schema: Schema
    ): AverageColorExtractor {
        val field = schema.get(parameters["field"] as String) as Schema.Field<ImageContent, FloatVectorDescriptor>
        val persisting = true
        return AverageColorExtractor(field, input, persisting)
    }
}