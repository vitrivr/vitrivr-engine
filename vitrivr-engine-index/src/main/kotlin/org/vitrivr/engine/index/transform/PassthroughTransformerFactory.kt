package org.vitrivr.engine.index.transform

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.TransformerFactory
import org.vitrivr.engine.core.operators.ingest.templates.DummyTransformer

class PassthroughTransformerFactory : TransformerFactory {
    override fun newOperator(
        input: Operator<ContentElement<*>>,
        parameters: Map<String, Any>,
        schema: Schema
    ): Transformer {
        return PassthroughTransformer(input, parameters)
    }
}