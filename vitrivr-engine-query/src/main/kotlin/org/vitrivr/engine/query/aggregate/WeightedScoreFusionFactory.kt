package org.vitrivr.engine.query.aggregate

import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Aggregator

val logger = KotlinLogging.logger {}

class WeightedScoreFusionFactory : OperatorFactory {
    override fun newOperator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        context: Context
    ): Aggregator {
        val weights = context[name, "weights"]?.split(",")?.mapNotNull { s -> s.trim().toDoubleOrNull() } ?: emptyList()
        val p = context[name, "p"]?.toDoubleOrNull() ?: 1.0
        val normalize = context[name, "normalize"]?.toBoolean() ?: true
        if (p == Double.POSITIVE_INFINITY && weights.isNotEmpty()) {
            logger.warn { "Weights are ignored when p is set to infinity" }
        }
        return WeightedScoreFusion(inputs.values.toList(), weights, p, normalize, name)
    }
}