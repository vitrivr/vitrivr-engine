package org.vitrivr.engine.query.aggregate

import io.github.oshai.kotlinlogging.KotlinLogging
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

    private val logger = KotlinLogging.logger {}

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
        logger.debug { "IntersectionAggregator '$name' starting with ${inputs.size} input operator(s)." }

        // If there are no inputs, return an empty flow
        if (inputs.isEmpty()) {
            logger.debug { "IntersectionAggregator '$name': No inputs provided, returning empty flow." }
            return emptyFlow()
        }

        // If there's only one input, return its flow
        if (inputs.size == 1) {
            logger.debug { "IntersectionAggregator '$name': Only one input provided, passing through results directly (with limit)." }
            return inputs.first().toFlow(scope)
        }

        return flow {
            logger.trace { "IntersectionAggregator '$name': Starting to collect results from all ${inputs.size} inputs." }
            // Collect results from all input operators
            val inputResults = inputs.map { it.toFlow(scope).toList() }

            logger.debug { "IntersectionAggregator '$name': Collected results from inputs. Sizes: ${inputResults.map { it.size }}" }

            // Check if all inputs have results
            if (inputResults.any { it.isEmpty() }) {
                logger.info { "IntersectionAggregator '$name': At least one input stream was empty. The final intersection is empty." }
                // If any input has no results, the intersection is empty
                return@flow
            }

            // Create a set of IDs for each input
            val idSets = inputResults.map { results ->
                results.map { it.id }.toSet()
            }

            logger.trace { "IntersectionAggregator '$name': Created ${idSets.size} sets of IDs for intersection." }


            // Find the intersection of all ID sets
            val commonIds = idSets.reduce { acc, ids -> acc.intersect(ids) }

            logger.info { "IntersectionAggregator '$name': Intersection of ${idSets.size} sets resulted in ${commonIds.size} common items." }
            logger.trace { "IntersectionAggregator '$name': Common IDs are: $commonIds" }

            // Create a map of all retrievables by ID for easy lookup
            val allRetrievablesById = mutableMapOf<RetrievableId, Retrievable>()
            for (results in inputResults) {
                for (result in results) {
                    // We'll use the first occurrence of each ID
                    if (!allRetrievablesById.containsKey(result.id)) {
                        allRetrievablesById[result.id] = result
                    }
                }
            }

            logger.trace { "IntersectionAggregator '$name': Created lookup map with ${allRetrievablesById.size} unique retrievables." }

            logger.debug { "IntersectionAggregator '$name': Emitting ${commonIds.size} final retrievables." }
            // Emit retrievables for the IDs in the intersection
            for (id in commonIds) {
                allRetrievablesById[id]?.let { emit(it) }
            }
        }
    }
}
