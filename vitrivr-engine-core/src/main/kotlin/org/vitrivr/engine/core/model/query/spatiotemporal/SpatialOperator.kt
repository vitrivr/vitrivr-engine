package org.vitrivr.engine.core.model.query.spatiotemporal

/**
 * An enum of available spatial operators.
 */
enum class SpatialOperator {
    /** Checks if a geometry is within a certain distance of another. Requires a distance parameter. */
    DWITHIN,

    /** Checks if two geometries intersect. */
    INTERSECTS,

    /** Checks if the first geometry contains the second. */
    CONTAINS,

    /** Checks if the first geometry is within the second. */
    WITHIN,

    /** Checks if two geometries are spatially equal. */
    EQUALS
}