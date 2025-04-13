package org.vitrivr.engine.query.aggregate

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import org.vitrivr.engine.core.operators.general.AggregatorFactory

class TemporalSequenceAggregatorFactory : AggregatorFactory {
    override fun newAggregator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        properties: Map<String, String>
    ): Aggregator {
        return TemporalSequenceAggregator(
            inputs, name
        )
    }

}