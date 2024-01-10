package org.vitrivr.engine.query.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

class ScoreAggregator(
    override val input: Operator<Retrieved>,
    private val aggregationMode: AggregationMode = AggregationMode.MAX,
    val relationshps: Set<String> = setOf("partOf")
) : Transformer<Retrieved, Retrieved.RetrievedWithScore> {

    enum class AggregationMode {
        MAX,
        MEAN,
        MIN
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved.RetrievedWithScore> =
        input.toFlow(scope).map { retrieved ->
            when (retrieved) {
                is Retrieved.RetrievedWithScore -> retrieved//pass through
                is Retrieved.RetrievedWithRelationship -> { //aggregate

                    val scores =
                        retrieved.relationships.filter { rel -> rel.pred in this.relationshps && rel.obj.first == retrieved.id }
                            .map { if (it.sub.second is Retrieved.RetrievedWithScore) (it.sub.second as Retrieved.RetrievedWithScore).score else 0f }

                    val score = if (scores.isEmpty()) {
                        0f
                    } else {
                        when (aggregationMode) {
                            AggregationMode.MAX -> scores.max()
                            AggregationMode.MEAN -> scores.sum() / scores.size
                            AggregationMode.MIN -> scores.min()
                        }
                    }

                    Retrieved.PlusScore(retrieved, score)

                }

                else -> Retrieved.PlusScore(retrieved, 0f)
            }
        }

}