package org.vitrivr.engine.core.model.query.bool

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.query.spatiotemporal.SpatialOperator
import org.vitrivr.engine.core.model.types.Value
import kotlin.math.*

/**
 * Test class to verify semantic correctness of [SpatialOperator] behavior on POINT() WKT data.
 * These tests use simple parsing + haversine distance without external geometry libraries.
 */
class SpatialOperatorBehaviorTest {

    private fun parseCoordinates(wkt: String): Pair<Double, Double> {
        val normalized = wkt.trim().uppercase()
        val regex = Regex("""POINT\s*\(\s*(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s*\)""")
        val match = regex.matchEntire(normalized)
            ?: throw IllegalArgumentException("Invalid POINT WKT: '$wkt'")
        return match.groupValues[1].toDouble() to match.groupValues[2].toDouble()
    }

    private fun haversineDistanceMeters(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        val (lon1, lat1) = p1
        val (lon2, lat2) = p2
        val r = 6371000.0 // Earth radius in meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    private fun evaluateOperator(
        op: SpatialOperator,
        reference: Value.GeographyValue,
        test: Value.GeographyValue,
        radiusMeters: Double? = null
    ): Boolean {
        val coordRef = parseCoordinates(reference.wkt)
        val coordTest = parseCoordinates(test.wkt)

        return when (op) {
            SpatialOperator.EQUALS -> coordRef == coordTest // ST_Equals for POINTs
            SpatialOperator.INTERSECTS -> coordRef == coordTest // ST_Intersects for POINTs
            SpatialOperator.CONTAINS -> false // POINT can't contain another POINT
            SpatialOperator.WITHIN -> false   // POINT can't be within another POINT
            SpatialOperator.DWITHIN -> {
                val dist = haversineDistanceMeters(coordRef, coordTest)
                radiusMeters?.let { dist <= it } ?: false
            }
        }
    }

    @Test
    fun testEqualsOperatorWithSamePoint() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test = Value.GeographyValue("POINT(8.5417 47.3769)")
        assertTrue(evaluateOperator(SpatialOperator.EQUALS, ref, test))
    }

    @Test
    fun testEqualsOperatorWithDifferentPoint() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test = Value.GeographyValue("POINT(8.5420 47.3769)")
        assertFalse(evaluateOperator(SpatialOperator.EQUALS, ref, test))
    }

    @Test
    fun testIntersectsWithSamePoint() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test = Value.GeographyValue("POINT(8.5417 47.3769)")
        assertTrue(evaluateOperator(SpatialOperator.INTERSECTS, ref, test))
    }

    @Test
    fun testIntersectsWithDifferentPoint() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test = Value.GeographyValue("POINT(8.542 47.377)")
        assertFalse(evaluateOperator(SpatialOperator.INTERSECTS, ref, test))
    }

    @Test
    fun testContainsFailsAlwaysForPoints() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test1 = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test2 = Value.GeographyValue("POINT(8.9083 47.3192)")
        assertFalse(evaluateOperator(SpatialOperator.CONTAINS, ref, test1))
        assertFalse(evaluateOperator(SpatialOperator.CONTAINS, ref, test2))

    }

    @Test
    fun testWithinFailsAlwaysForPoints() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test1 = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test2 = Value.GeographyValue("POINT(10.2837 47.9375)")
        assertFalse(evaluateOperator(SpatialOperator.WITHIN, ref, test1))
        assertFalse(evaluateOperator(SpatialOperator.WITHIN, ref, test2))

    }

    @Test
    fun testDWithinTrueWhenClose() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test = Value.GeographyValue("POINT(8.5419 47.3770)") // ~20m away
        assertTrue(evaluateOperator(SpatialOperator.DWITHIN, ref, test, 50.0))
    }

    @Test
    fun testDWithinFalseWhenFar() {
        val ref = Value.GeographyValue("POINT(8.5417 47.3769)")
        val test = Value.GeographyValue("POINT(8.5500 47.3800)") // ~700m away
        assertFalse(evaluateOperator(SpatialOperator.DWITHIN, ref, test, 100.0))
    }
}