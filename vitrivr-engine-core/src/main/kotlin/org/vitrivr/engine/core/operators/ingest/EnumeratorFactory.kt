package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.source.Source

interface EnumeratorFactory : OperatorFactory<Operator<Nothing>, Enumerator>{
    fun newOperator(parameters: Map<String, Any>): Enumerator

    override fun newOperator(input: Operator<Nothing>, parameters: Map<String, Any>): Enumerator {
        return newOperator(parameters)
    }
}