package org.vitrivr.engine.core.operators

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema

interface OperatorFactory <I : Operator<*>, O : Operator<*>> {
    fun  newOperator(input: I, parameters: Map<String,Any> = emptyMap(), schema: Schema, context: IndexContext) : O
}