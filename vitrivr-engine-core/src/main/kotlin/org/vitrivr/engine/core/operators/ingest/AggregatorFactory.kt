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
     * @param name The name of the [Aggregator]
     * @param input The input [Segmenter].
     * @param context The [IndexContext] to use.
     */
    fun newOperator(name: String, input: Segmenter, context: IndexContext): Aggregator
}
