package org.vitrivr.engine.core.operators.general

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A factory object for a specific [Exporter] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface ExporterFactory {
    /**
     * Creates a new [Exporter] instance from this [ExporterFactory].
     *
     * @param name The name of the [Exporter]
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     */
    fun newExporter(name: String, input: Operator<Retrievable>, context: IndexContext): Exporter
}
