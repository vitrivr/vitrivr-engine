package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator
import kotlin.math.pow

class WeightedScoreFusion(
    override val inputs: List<Operator<out Retrievable>>,
    weights: List<Double>,
    private val p: Double,
    private val normalize: Boolean,
    override val name: String
) : Aggregator {

    private val weights: List<Double> = when {
        weights.size > inputs.size -> weights.subList(0, inputs.size - 1)
        weights.size < inputs.size -> weights + List(inputs.size - weights.size) { 1.0 }
        else -> weights
    }

    private val weightsSum = this.weights.sum()

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {

        if (inputs.isEmpty()) {
            return emptyFlow()
        }

        if (inputs.size == 1) {
            return inputs.first().toFlow(scope)
        }

        return flow {

            val inputs = inputs.map { it.toFlow(scope).toList() }

            // Check if there is more than one populated input, return early if not
            if (inputs.filter { it.isNotEmpty() }.size < 2) {
                inputs.asSequence().flatten().forEach { emit(it) }
                return@flow
            }

            val scoreMap = mutableMapOf<RetrievableId, MutableList<Pair<Int, Retrievable>>>()

            for ((index, retrieveds) in inputs.withIndex()) {
                for (retrieved in retrieveds) {
                    scoreMap.computeIfAbsent(retrieved.id) { mutableListOf() }
                    scoreMap[retrieved.id]!!.add(index to retrieved)
                }
            }

            for ((_, retrieveds) in scoreMap) {
                var score: Double
                val first: Retrievable

                if (p == Double.POSITIVE_INFINITY) {
                    // Max score selection when p = infinity
                    score = retrieveds.maxOfOrNull {
                        (it.second.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0)
                    } ?: 0.0

                    first = retrieveds.first().second
                } else {
                    // Compute weighted p-norm score without normalization
                    score = retrieveds.map {
                        (it.second.filteredAttribute(ScoreAttribute::class.java)?.score ?: 0.0).pow(p) * weights[it.first]
                    }.sum().pow(1 / p)

                    first = retrieveds.first().second
                }

                // Create a copy and override the score
                emit(first.copy(attributes = first.attributes + ScoreAttribute.Unbound(score)))
            }
        }
    }
}
