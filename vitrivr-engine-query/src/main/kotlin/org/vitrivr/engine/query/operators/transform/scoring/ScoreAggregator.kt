package org.vitrivr.engine.query.operators.transform.scoring

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer

class ScoreAggregator(
    override val input: Operator<out Retrievable>,
    private val aggregationMode: AggregationMode = AggregationMode.MAX,
    private val relations: Set<String> = setOf("partOf"),
    override val name: String
) : Transformer {
    enum class AggregationMode {
        MAX,
        MEAN,
        MIN
    }

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map { retrieved ->
        if (retrieved.filteredAttribute(ScoreAttribute::class.java) != null) { //pass through if score is already set
            return@map retrieved
        }
        val relationships = retrieved.relationships.filterIsInstance<Relationship.ByRef>()
        if (relationships.isNotEmpty()) {
            val scores = relationships.filter { rel -> rel.predicate in this.relations && rel.objectId == retrieved.id }.map { it.subject.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0 }
            val score = if (scores.isEmpty()) {
                0.0
            } else {
                when (aggregationMode) {
                    AggregationMode.MAX -> scores.max()
                    AggregationMode.MEAN -> scores.sum() / scores.size
                    AggregationMode.MIN -> scores.min()
                }
            }
            retrieved.copy(attributes = retrieved.attributes + ScoreAttribute.Unbound(score))
        } else {
            retrieved.copy(attributes = retrieved.attributes + ScoreAttribute.Unbound(0.0))
        }
    }
}