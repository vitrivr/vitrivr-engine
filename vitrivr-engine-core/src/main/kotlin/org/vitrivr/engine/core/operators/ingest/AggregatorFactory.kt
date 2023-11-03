package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext

/**
 * A factory object for a specific [Aggregator] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface AggregatorFactory {
    /**
     * Creates a new [Aggregator] instance from this [AggregatorFactory].
     *
     * @param input The input [Segmenter].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    fun newOperator(input: Segmenter, context: IndexContext, parameters: Map<String, String> = emptyMap()): Aggregator
}