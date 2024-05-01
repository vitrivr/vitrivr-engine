package org.vitrivr.engine.query.aggregate

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import org.vitrivr.engine.core.operators.general.AggregatorFactory

class TemporalSequenceAggregatorFactory : AggregatorFactory {
    override fun newAggregator(
        name: String,
        inputs: List<Operator<Retrievable>>,
        context: Context
    ): Aggregator {
        return TemporalSequenceAggregator(
            inputs
        )
    }
}