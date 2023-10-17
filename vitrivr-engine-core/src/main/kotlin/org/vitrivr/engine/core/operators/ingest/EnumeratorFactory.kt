package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.source.Source

interface EnumeratorFactory : OperatorFactory<Operator<Nothing>, Enumerator> {
    fun newOperator(parameters: Map<String, Any>, schema: Schema): Enumerator

    override fun newOperator(input: Operator<Nothing>, parameters: Map<String, Any>, schema: Schema): Enumerator {
        return newOperator(parameters, schema)
    }
}