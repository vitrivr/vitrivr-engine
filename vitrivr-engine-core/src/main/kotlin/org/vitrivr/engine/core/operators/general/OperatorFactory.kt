package org.vitrivr.engine.core.operators.general

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

interface OperatorFactory {

    fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context) : Operator<out Retrievable>

}