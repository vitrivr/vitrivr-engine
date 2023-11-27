package org.vitrivr.engine.query.transform

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer
import org.vitrivr.engine.core.operators.retrieve.TransformerFactory

class ScoreAggregatorFactory : TransformerFactory<Retrieved, Retrieved.RetrievedWithScore> {
    override fun newTransformer(
        input: Operator<Retrieved>,
        schema: Schema,
        properties: Map<String, String>
    ): Transformer<Retrieved, Retrieved.RetrievedWithScore> {

        val aggregation = properties["aggregation"]?.uppercase()?.let {
            try {
                ScoreAggregator.AggregationMode.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        } ?: ScoreAggregator.AggregationMode.MAX

        val relationships = properties["relationships"]?.split(",")?.map { s -> s.trim() }?.toSet() ?: setOf("partOf")

        return ScoreAggregator(input, aggregation, relationships)
    }
}