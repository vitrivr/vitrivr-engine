package org.vitrivr.engine.base.features.external

import org.vitrivr.engine.base.features.averagecolor.AverageColor
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

abstract class ExternalImageExtractor : Extractor<ImageContent, FloatVectorDescriptor> {

   val DEFAULT_API_ENDPOINT = "http://localhost:8888"

}