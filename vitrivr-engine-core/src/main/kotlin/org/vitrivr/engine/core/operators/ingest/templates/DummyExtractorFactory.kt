package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.ingest.ExtractorFactory

class DummyExtractorFactory : ExtractorFactory {

    override fun newOperator(input: Operator<Ingested>, parameters: Map<String, Any>): Extractor<ContentElement<*>, Descriptor>  {
        return DummyExtractor(input, parameters)
    }
}