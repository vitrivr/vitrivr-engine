package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Aggregator

/**
 * An [Aggregator] that performs an intersection operation on the results from multiple input operators.
 * It only yields items that appear in all input operators, effectively implementing a logical AND operation for query results.
 *
 * @param inputs The list of input operators to aggregate.
 * @param name The name of this aggregator.
 *
 * @author henrikluemkemann
 * @version 1.0.0
 */
class IntersectionAggregator(
    override val inputs: List<Operator<out Retrievable>>,
    override val name: String
) : Aggregator {

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
        // If there are no inputs, return an empty flow
        if (inputs.isEmpty()) {
            return emptyFlow()
        }

        // If there's only one input, return its flow
        if (inputs.size == 1) {
            return inputs.first().toFlow(scope)
        }

        // if more than one input:
        return flow {
            // Collect results from all input operators
            val inputResults = inputs.map { it.toFlow(scope).toList() }

            // check if all inputs have results
            if (inputResults.any { it.isEmpty() }) {
                // If any input has no results, the intersection is empty
                return@flow
            }

            // Group results by ID
            val resultsByIdMap = mutableMapOf<RetrievableId, MutableList<Retrievable>>()
            
            // Populate the map with results from all inputs
            for (results in inputResults) {
                for (result in results) {
                    resultsByIdMap.computeIfAbsent(result.id) { mutableListOf() }.add(result)
                }
            }
            
            // Find IDs that appear in all inputs
            val intersectionIds = resultsByIdMap.filter { (_, retrievables) -> 
                // Count unique input indices
                val uniqueInputIndices = retrievables.mapNotNull { retrievable ->
                    // find which input this retrievable came from
                    inputResults.indexOfFirst { inputList -> 
                        inputList.any { it.id == retrievable.id }
                    }
                }.toSet()
                
                // Check if the retrievable appears in all inputs
                uniqueInputIndices.size == inputs.size
            }.keys
            
            // emit the first occurrence of each retrievable in the intersection
            for (id in intersectionIds) {
                emit(resultsByIdMap[id]!!.first())
            }
        }
    }
}