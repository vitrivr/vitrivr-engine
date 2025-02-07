package org.vitrivr.engine.core.operators.general

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A factory object for a specific [Processor] type.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface ProcessorFactory {
    /**
     * Creates a new [Processor] instance from this [ProcessorFactory].
     *
     * @param name The name of the [Processor]
     * @param input The input [Operator].
     * @param context The [Context] to use.
     */
    fun newProcessor(name: String, input: Operator<Retrievable>, context: Context): Processor
}