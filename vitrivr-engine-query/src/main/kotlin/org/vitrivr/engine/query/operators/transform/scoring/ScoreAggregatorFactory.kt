package org.vitrivr.engine.query.operators.transform.scoring

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory


class ScoreAggregatorFactory : TransformerFactory {
    override fun newTransformer(
        name: String,
        input: Operator<out Retrievable>,
        schema: Schema,
        properties: Map<String, String>
    ): Transformer {

        val aggregation = properties["aggregation"]?.uppercase()?.let {
            try {
                ScoreAggregator.AggregationMode.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        } ?: ScoreAggregator.AggregationMode.MAX

        val relationships = properties["relationships"]?.split(",")?.map { s -> s.trim() }?.toSet() ?: setOf("partOf")

        return ScoreAggregator(input, aggregation, relationships, name)
    }
}