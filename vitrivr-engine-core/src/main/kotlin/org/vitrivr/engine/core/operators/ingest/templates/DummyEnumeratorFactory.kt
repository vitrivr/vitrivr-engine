package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.Source

class DummyEnumeratorFactory : EnumeratorFactory {
    override fun newOperator(parameters: Map<String, Any>, schema: Schema, context: Context): Enumerator {

        return DummyEnumerator(parameters)
    }
}