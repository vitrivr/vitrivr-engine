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
import java.util.*
import kotlin.math.pow

class WeightedScoreFusion(
    override val inputs: List<Operator<out Retrievable>>,
    weights: List<Float>,
    val p: Float,
    val normalize: Boolean
) : Aggregator {

    private val weights: List<Float> = when {
        weights.size > inputs.size -> weights.subList(0, inputs.size - 1)
        weights.size < inputs.size -> weights + List(inputs.size - weights.size) {1f}
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

            //check if there is more than one populated input, return early if not
            if (inputs.filter { it.isNotEmpty() }.size < 2) {
                inputs.asSequence().flatten().forEach { emit(it) }
                return@flow
            }

            val scoreMap = mutableMapOf<RetrievableId, MutableList<Pair<Int, Retrievable>>>()

            for ((index, retrieveds) in inputs.withIndex()) {

                for (retrieved in retrieveds) {

                    if (!scoreMap.containsKey(retrieved.id)) {
                        scoreMap[retrieved.id] = mutableListOf()
                    }

                    scoreMap[retrieved.id]!!.add(index to retrieved)

                }

            }

            for((_, retrieveds) in scoreMap) {

                var score : Float
                var first : Retrievable

                if (p == Float.POSITIVE_INFINITY){
                    score = retrieveds.map { ((it.second.filteredAttribute(ScoreAttribute::class.java))?.score ?: 0f) }.max()

                    first = retrieveds.first().second
                }
                else{
                    val normalization = (retrieveds.map { weights[it.first] }.sum()).pow(1/p)

                    if (normalization == 0f){
                        score = 0f
                    }
                    else {
                        score = retrieveds.map {
                            ((it.second.filteredAttribute(ScoreAttribute::class.java))?.score ?: 0f).pow(p) * weights[it.first]
                        }.sum().pow(1 / p)
                        if (normalize) score /= normalization
                    }
                    first = retrieveds.first().second
                }

                //make a copy and override score
                val retrieved = first.copy()
                retrieved.filteredAttribute(ScoreAttribute::class.java)
                retrieved.addAttribute(ScoreAttribute.Unbound(score))

                emit(retrieved)

            }

        }
    }
}