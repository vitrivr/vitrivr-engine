package org.vitrivr.engine.query.aggregate

import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import org.vitrivr.engine.core.operators.general.AggregatorFactory

val logger = KotlinLogging.logger {}

class WeightedScoreFusionFactory : AggregatorFactory {
    override fun newAggregator(
        name: String,
        inputs: List<Operator<out Retrievable>>,
        context: Context
    ): Aggregator {
        val weights = context[name, "weights"]?.split(",")?.mapNotNull { s -> s.trim().toFloatOrNull() } ?: emptyList()
        val p = context[name, "p"]?.toFloatOrNull() ?: 1f
        val normalize = context[name, "normalize"]?.toBoolean() ?: true
        if (p == Float.POSITIVE_INFINITY && weights.isNotEmpty()) {
            logger.warn { "Weights are ignored when p is set to infinity" }
        }
        return WeightedScoreFusion(inputs, weights, p, normalize)
    }
}