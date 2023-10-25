package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

interface EnumeratorFactory : OperatorFactory<Operator<Nothing>, Enumerator> {
    fun newOperator(parameters: Map<String, Any>, schema: Schema, context: IndexContext): Enumerator

    override fun newOperator(
        input: Operator<Nothing>,
        parameters: Map<String, Any>,
        schema: Schema,
        context: IndexContext
    ): Enumerator {
        return newOperator(parameters, schema, context)
    }
}