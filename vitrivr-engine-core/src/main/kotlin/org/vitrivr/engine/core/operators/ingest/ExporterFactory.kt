package org.vitrivr.engine.core.operators.ingest

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
     * @param input The input [Enumerator].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    fun newOperator(input: Operator<Retrievable>, context: IndexContext, parameters: Map<String, String>): Exporter
}