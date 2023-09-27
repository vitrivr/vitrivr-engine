package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.operators.ingest.Extractor

abstract class ExternalImageExtractor : Extractor<ImageContent, FloatVectorDescriptor> {

   val DEFAULT_API_ENDPOINT = "http://localhost:8888"

}