package org.vitrivr.engine.query.aggregate

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import org.vitrivr.engine.core.operators.general.AggregatorFactory

/**
 * Factory for creating [IntersectionAggregator] instances.
 *
 * @author henrikluemkemann
 * @version 1.0.0
 */
class IntersectionAggregatorFactory : AggregatorFactory {

    /**
     * Creates a new [IntersectionAggregator] instance.
     *
     * @param name The name of the aggregator.
     * @param inputs The list of input operators to aggregate.
     * @param context The context for the aggregator.
     * @return A new [IntersectionAggregator] instance.
     */
    override fun newAggregator(
        name: String,
        inputs: List<Operator<out Retrievable>>,
        context: Context
    ): Aggregator {
        return IntersectionAggregator(
            inputs, name
        )
    }
}