package org.vitrivr.engine.core.model.query.geography

import org.vitrivr.engine.core.model.types.Value
import kotlinx.serialization.Serializable

/**
 * Represents a single spatial condition to be applied to a geography attribute.
 *
 * @param attributeName The name of the schema attribute (of Type.Geography) to query.
 * @param operator The spatial operator to apply.
 * @param referenceGeography The reference geography value (e.g., a point, polygon WKT) for the comparison.
 * @param distance The distance value, required for operators like DWITHIN. Assumed to be in meters for PostGIS geography.
 * @param useSpheroid For DWITHIN on geography types, whether to use spheroid-based calculation (more accurate, slower) or planar (faster, less accurate over large distances). Defaults to true.
 */
@Serializable
data class SpatialCondition(
    val attributeName: String,
    val operator: SpatialOperator,
    val referenceGeography: Value.GeographyValue, // The WKT point/polygon etc. to compare against
    val distance: Value.Double? = null,          // For DWITHIN
    val useSpheroid: Value.Boolean? = null       // For DWITHIN, defaults to true for geography
)