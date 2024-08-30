package org.vitrivr.engine.query.operators.transform.scoring

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import kotlin.math.pow

class ScoreExaggerator(override val input: Operator<out Retrievable>, val factor: Float) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
        return flow {
            input.toFlow(scope).collect { retrieved : Retrievable ->

                val newAttributes = mutableListOf<ScoreAttribute>()
                retrieved.filteredAttributes(ScoreAttribute::class.java).forEach { attribute ->
                    when(attribute) {
                        is ScoreAttribute.Similarity -> {
                            val newScore = exaggerateSimilarity(attribute.score)
                            newAttributes.add(ScoreAttribute.Similarity(newScore))
                        }
                        is ScoreAttribute.Unbound -> {
                            val newScore = exaggerateUnbound(attribute.score)
                            newAttributes.add(ScoreAttribute.Unbound(newScore))
                        }
                    }
                }
                retrieved.removeAttributes(ScoreAttribute::class.java)
                newAttributes.forEach { retrieved.addAttribute(it) }
                emit(retrieved)
            }
        }
    }

    private fun exaggerateSimilarity(score: Float): Float {
        // -1 + 2/(1 + ((-1 - x)/(-1 + x))^(-a))
        return -1 + 2 / (1 + ((-1 - score) / (-1 + score)).pow(-factor))
    }

    private fun exaggerateUnbound(score: Float): Float {
        return score * factor
    }

}