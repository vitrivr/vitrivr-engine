package org.vitrivr.engine.query.aggregate

import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import org.vitrivr.engine.core.operators.general.AggregatorFactory

val logger = KotlinLogging.logger {}

class WeightedScoreFusionFactory : AggregatorFactory {
    override fun newAggregator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        properties: Map<String, String>
    ): Aggregator {
        val weights = properties["weights"]?.split(",")?.mapNotNull { s -> s.trim().toDoubleOrNull() } ?: emptyList()
        val p = properties["p"]?.toDoubleOrNull() ?: 1.0
        val normalize = properties["normalize"]?.toBoolean() != false
        if (p == Double.POSITIVE_INFINITY && weights.isNotEmpty()) {
            logger.warn { "Weights are ignored when p is set to infinity" }
        }
        return WeightedScoreFusion(inputs, weights, p, normalize, name)
    }

}