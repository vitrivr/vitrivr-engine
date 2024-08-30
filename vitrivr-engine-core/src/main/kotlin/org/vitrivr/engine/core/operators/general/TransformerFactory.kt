package org.vitrivr.engine.core.operators.general

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A factory object for a specific [Transformer] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface TransformerFactory {
    /**
     * Creates a new [Transformer] instance from this [TransformerFactory].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator].
     * @param context The [Context] to use.
     */
    fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer
}
