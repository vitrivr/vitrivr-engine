package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.operators.ingest.SegmenterFactory

class DummySegmenterFactory : SegmenterFactory {

    override fun newOperator(
        input: Operator<ContentElement<*>>,
        parameters: Map<String, Any>,
        schema: Schema
    ): Segmenter {
        return DummySegmenter(input, parameters)
    }
}