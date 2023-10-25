package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory

class DummyEnumeratorFactory : EnumeratorFactory {
    override fun newOperator(parameters: Map<String, Any>, schema: Schema, context: IndexContext): Enumerator {

        return DummyEnumerator(parameters)
    }
}