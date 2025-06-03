// Suggested location: org/vitrivr/engine/core/model/query/spatial/SpatialOperator.kt
package org.vitrivr.engine.core.model.query.geography

import kotlinx.serialization.Serializable

/**
 * Enumerates specific spatial comparison operators that typically map to PostGIS functions.
 */
@Serializable
enum class SpatialOperator {
    /** Checks if a geometry is within a certain distance of another (e.g., ST_DWithin). */
    DWITHIN,
    /** Checks if two geometries spatially intersect (e.g., ST_Intersects). */
    INTERSECTS,
    /** Checks if geometry A spatially contains geometry B (e.g., ST_Contains). */
    CONTAINS,
    /** Checks if geometry A is spatially within geometry B (e.g., ST_Within). */
    WITHIN,
    /** Checks if two geometries are spatially equal (e.g., ST_Equals). */
    EQUALS;
    // Add more as needed, e.g., OVERLAPS, TOUCHES, CROSSES, COVERS, COVEREDBY
}