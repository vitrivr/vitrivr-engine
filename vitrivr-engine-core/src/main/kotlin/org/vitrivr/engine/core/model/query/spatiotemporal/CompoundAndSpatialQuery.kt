// Suggested location: org/vitrivr/engine/core/model/query/spatial/CompoundAndSpatialQuery.kt
package org.vitrivr.engine.core.model.query.spatiotemporal

import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import kotlinx.serialization.Serializable

/**
 * A query type that combines multiple [SpatialCondition]s using a logical AND.
 * All conditions must be met for a retrievable to match.
 *
 * @param conditions A list of spatial conditions to apply.
 * @param relevantRetrievableIds Optional set of [RetrievableId]s to restrict the query scope.
 */
@Serializable // If you need to serialize this query
data class CompoundAndSpatialQuery(
    val conditions: List<SpatialCondition>,
    val relevantRetrievableIds: Set<RetrievableId>? = null // From base Query interface
) : Query { // Ensure 'Query' is the correct base interface from your project
    init {
        require(conditions.isNotEmpty()) { "CompoundAndSpatialQuery must have at least one condition." }
    }
}