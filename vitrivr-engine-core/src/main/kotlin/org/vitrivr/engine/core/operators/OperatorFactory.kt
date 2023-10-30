package org.vitrivr.engine.core.operators

import org.vitrivr.engine.core.context.IndexContext

/**
 * A factory object for a specific [Operator] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface OperatorFactory <I : Operator<*>, O : Operator<*>> {
    fun newOperator(input: I, context: IndexContext, parameters: Map<String, Any> = emptyMap()): O
}