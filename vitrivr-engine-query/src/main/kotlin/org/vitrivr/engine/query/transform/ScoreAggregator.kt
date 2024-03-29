package org.vitrivr.engine.query.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.RelationshipAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

class ScoreAggregator(
    override val input: Operator<Retrieved>,
    private val aggregationMode: AggregationMode = AggregationMode.MAX,
    private val relations: Set<String> = setOf("partOf")
) : Transformer {

    enum class AggregationMode {
        MAX,
        MEAN,
        MIN
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> =
        input.toFlow(scope).map { retrieved ->

            if (retrieved.filteredAttribute<ScoreAttribute>() != null) { //pass through if score is already set
                return@map retrieved
            }

            val relationships = retrieved.filteredAttribute<RelationshipAttribute>()?.relationships ?: emptySet()

            if (relationships.isNotEmpty()) {

                val scores =
                    relationships.filter { rel -> rel.pred in this.relations && rel.obj.first == retrieved.id }
                        .map { it.sub.second?.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0f }

                val score = if (scores.isEmpty()) {
                    0f
                } else {
                    when (aggregationMode) {
                        AggregationMode.MAX -> scores.max()
                        AggregationMode.MEAN -> scores.sum() / scores.size
                        AggregationMode.MIN -> scores.min()
                    }
                }

                retrieved.addAttribute(ScoreAttribute(score))

            } else {
                retrieved.addAttribute(ScoreAttribute(0f))
            }

            retrieved
        }

}