package org.vitrivr.engine.core.operators

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * A factors that creates [Operator]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
interface OperatorFactory {
    /**
     * Creates and returns a new [Operator] instance from this [OperatorFactory].
     *
     * @param name The name of the [Operator] to create.
     * @param inputs A [Map] of the named input [Operator]s.
     * @param context The [Context] to use.
     */
    fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context) : Operator<out Retrievable>

    /**
     * Creates and returns a new [Operator] instance from this [OperatorFactory].
     *
     * @param name The name of the [Operator] to create.
     * @param input Input [Operator]; will be labeled 'input'.
     * @param context The [Context] to use.
     */
    fun newOperator(name: String, input: Operator<out Retrievable>, context: Context) : Operator<out Retrievable> = newOperator(name, mapOf("input" to input), context)

    /**
     * Creates and returns a new [Operator] instance from this [OperatorFactory].
     *
     * @param name The name of the [Operator] to create.
     * @param context The [Context] to use.
     */
    fun newOperator(name: String, context: Context) : Operator<out Retrievable> = newOperator(name, emptyMap(), context)
}