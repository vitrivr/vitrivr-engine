package org.vitrivr.engine.query.operators.transform.scoring

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrieved
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

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = this.input.toFlow(scope).map { retrieved ->
        if (retrieved.filteredAttribute<ScoreAttribute>() != null) { //pass through if score is already set
            return@map retrieved
        }

        val relationships = retrieved.relationships.filterIsInstance<Relationship.ByRef>()
        if (relationships.isNotEmpty()) {
            val scores = relationships.filter { rel -> rel.predicate in this.relations && rel.objectId == retrieved.id }.map { it.subject.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0f }
            val score = if (scores.isEmpty()) {
                0f
            } else {
                when (aggregationMode) {
                    AggregationMode.MAX -> scores.max()
                    AggregationMode.MEAN -> scores.sum() / scores.size
                    AggregationMode.MIN -> scores.min()
                }
            }
            retrieved.addAttribute(ScoreAttribute.Unbound(score))
        } else {
            retrieved.addAttribute(ScoreAttribute.Unbound(0f))
        }
        retrieved
    }
}