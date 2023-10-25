package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.TransformerFactory

class DummyTransformerFactory : TransformerFactory {

    override fun newOperator(
        input: Operator<ContentElement<*>>,
        parameters: Map<String, Any>,
        schema: Schema,
        context: IndexContext
    ): Transformer {
        return DummyTransformer(input, parameters)
    }
}