package org.vitrivr.engine.query.operators.transform.scoring

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer


class ScoreAggregatorFactory : OperatorFactory {
    override fun newOperator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        context: Context
    ): Transformer {

        val aggregation = context[name, "aggregation"]?.uppercase()?.let {
            try {
                ScoreAggregator.AggregationMode.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        } ?: ScoreAggregator.AggregationMode.MAX

        val relationships = context[name, "relationships"]?.split(",")?.map { s -> s.trim() }?.toSet() ?: setOf("partOf")

        return ScoreAggregator(inputs.values.first(), aggregation, relationships, name)
    }
}