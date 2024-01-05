package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Aggregator

class WeightedScoreFusion(
    override val inputs: List<Operator<Retrieved>>,
    weights: List<Float>
) : Aggregator<Retrieved, Retrieved> {

    private val weights: List<Float>
    init {
        this.weights = when {
            weights.size > inputs.size -> weights.subList(0, inputs.size - 1)
            weights.size < inputs.size -> weights + List(inputs.size - weights.size) {1f}
            else -> weights
        }
    }

    private val weightsSum = this.weights.sum()

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {

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

                val score = retrieveds.map { ((it.second as? Retrieved.RetrievedWithScore)?.score ?: 0f) * weights[it.first] }.sum() / weightsSum

                val first = retrieveds.first().second


                //TODO better merging with type/attribute preservation
                val retrieved = Retrieved.WithScore(
                    first.id,
                    first.type,
                    score,
                    false
                )

                emit(retrieved)

            }

        }
    }
}