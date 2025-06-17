package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.query.spatiotemporal.SpatialOperator
import org.vitrivr.engine.core.model.types.Value

/**
 * A [BooleanQuery] that checks a spatial relationship between a geography field and a reference geography.
 *
 * This query is translated into a database-specific spatial function call (e.g., PostGIS ST_DWithin).
 */
data class SpatialBooleanQuery(
    /** The name of the attribute (column) holding the latitude value. */
    val latAttribute: String? = null,

    /** The name of the attribute (column) holding the longitude value. */
    val lonAttribute: String? = null,

    /** The name of the single, native GEOGRAPHY attribute. Use this OR lat/lon attributes. */
    val attribute: String? = null,

    /** the spatial operator to use */
    val operator: SpatialOperator,

    /** The reference geography for the comparison (like the center of the circle). */
    val reference: Value.GeographyValue,

    /** The distance for the operator (radius in meters for DWITHIN). */
    val distance: Value.Double? = null,

    /** An optional parameter for certain operators (like use spheroid for DWITHIN). Defaults to true. */
    val useSpheroid: Value.Boolean? = Value.Boolean(true),

    override val limit: Long = Long.MAX_VALUE
) : BooleanQuery {

    /**
     * Validation block to ensure the query is constructed correctly.
     */
    init {
        val isNativeQuery = attribute != null
        val isStructQuery = latAttribute != null && lonAttribute != null

        // Enforce that it's either a native query OR a struct query, but not both or neither.
        require(isNativeQuery xor isStructQuery) {
            "SpatialBooleanQuery must be initialized with either a single 'attribute' (for a native geography type) OR both 'latAttribute' and 'lonAttribute' (for a struct type), but not both or neither."
        }
    }
}