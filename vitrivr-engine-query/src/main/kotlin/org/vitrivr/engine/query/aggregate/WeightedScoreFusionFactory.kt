package org.vitrivr.engine.query.aggregate

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import org.vitrivr.engine.core.operators.general.AggregatorFactory

class WeightedScoreFusionFactory : AggregatorFactory {
    override fun newAggregator(
        name: String,
        inputs: List<Operator<out Retrievable>>,
        context: Context
    ): Aggregator {
        val weights = context[name, "weights"]?.split(",")?.mapNotNull { s -> s.trim().toFloatOrNull() } ?: emptyList()
        return WeightedScoreFusion(inputs, weights)
    }
}