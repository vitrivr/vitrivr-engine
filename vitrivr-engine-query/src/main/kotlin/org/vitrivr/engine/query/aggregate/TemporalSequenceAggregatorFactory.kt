package org.vitrivr.engine.query.aggregate

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Aggregator

class TemporalSequenceAggregatorFactory : OperatorFactory {
    override fun newOperator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        context: Context
    ): Aggregator {
        return TemporalSequenceAggregator(
            inputs.values.toList(), name
        )
    }
}