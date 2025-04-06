package org.vitrivr.engine.core.operators.general

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

interface AggregatorFactory {

    fun newAggregator(name: String, inputs: Map<String, Operator<out Retrievable>>, properties: Map<String, String>): Aggregator

}