package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.operators.ingest.SegmenterFactory

class DummySegmenterFactory : SegmenterFactory {

    override fun newOperator(input: Operator<ContentElement<*>>, parameters: Map<String, Any>): Segmenter {
        return DummySegmenter(input, parameters)
    }
}