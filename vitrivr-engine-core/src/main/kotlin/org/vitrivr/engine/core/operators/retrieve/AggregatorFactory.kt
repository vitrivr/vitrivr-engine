package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

interface AggregatorFactory<I : Retrieved, O : Retrieved> {

    fun newAggregator(inputs: List<Operator<Retrieved>>, schema: Schema, properties: Map<String, String>) : Aggregator

}