package org.vitrivr.engine.query.aggregate

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Aggregator
import org.vitrivr.engine.core.operators.retrieve.AggregatorFactory

class TemporalSequenceAggregatorFactory : AggregatorFactory<Retrieved, Retrieved> {
    override fun newAggregator(
        inputs: List<Operator<Retrieved>>,
        schema: Schema,
        properties: Map<String, String>
    ): Aggregator {
        return TemporalSequenceAggregator(
            inputs
        )
    }
}