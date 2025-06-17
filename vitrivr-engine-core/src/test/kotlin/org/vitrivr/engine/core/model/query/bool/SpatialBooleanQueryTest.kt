package org.vitrivr.engine.core.model.query.bool

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.vitrivr.engine.core.model.query.spatiotemporal.SpatialOperator
import org.vitrivr.engine.core.model.types.Value

/**
 * Test class for [SpatialBooleanQuery] to verify its functionality with POINT geographies.
 */
class SpatialBooleanQueryTest {

    /**
     * Test the creation of a SpatialBooleanQuery with the DWITHIN operator for circular queries.
     */
    @Test
    fun testCreationWithDWithin() {
        val query = SpatialBooleanQuery(
            latAttribute = "latitude",
            lonAttribute = "longitude",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(8.5417 47.3769)"), // Zürich coordinates
            distance = Value.Double(1000.0), // 1km radius
            limit = 10
        )

        // Verify the query properties
        assertEquals("latitude", query.latAttribute)
        assertEquals("longitude", query.lonAttribute)
        assertEquals(SpatialOperator.DWITHIN, query.operator)
        assertEquals("POINT(8.5417 47.3769)", query.reference.wkt)
        assertEquals(4326, query.reference.srid)
        assertEquals(1000.0, query.distance?.value)
        assertEquals(true, query.useSpheroid?.value)
        assertEquals(10, query.limit)
        assertEquals(null, query.attribute)
    }

    /**
     * Tests the creation of a valid SpatialBooleanQuery for a NATIVE geography column.
     */
    @Test
    fun testCreationWithNativeAttribute() {
        val query = SpatialBooleanQuery(
            attribute = "location_geography", // native geography tpye
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(8.5417 47.3769)"),
            distance = Value.Double(500.0)
        )

        assertEquals("location_geography", query.attribute)
        assertEquals(SpatialOperator.DWITHIN, query.operator)
        assertEquals(500.0, query.distance?.value)

        assertEquals(null, query.latAttribute)
        assertEquals(null, query.lonAttribute)
    }

    /**
     * Test the creation of a SpatialBooleanQuery with the EQUALS operator for exact point matches.
     */
    @Test
    fun testCreationWithEquals() {
        val query = SpatialBooleanQuery(
            latAttribute = "latitude",
            lonAttribute = "longitude",
            operator = SpatialOperator.EQUALS,
            reference = Value.GeographyValue("POINT(-74.0060 40.7128)"),
            limit = 50
        )

        // Verify the query properties
        assertEquals("latitude", query.latAttribute)
        assertEquals("longitude", query.lonAttribute)
        assertEquals(SpatialOperator.EQUALS, query.operator)
        assertEquals("POINT(-74.0060 40.7128)", query.reference.wkt)
        assertEquals(null, query.distance) // Distance not used for EQUALS
        assertEquals(50, query.limit)
    }

    /**
     * Test the equality of SpatialBooleanQuery instances.
     */
    @Test
    fun testEquality() {
        // Create a base query
        val query1 = SpatialBooleanQuery(
            latAttribute = "lat",
            lonAttribute = "lon",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(8.5417 47.3769)"),
            distance = Value.Double(1000.0)
        )

        // Create an identical query
        val query2 = SpatialBooleanQuery(
            latAttribute = "lat",
            lonAttribute = "lon",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(8.5417 47.3769)"),
            distance = Value.Double(1000.0)
        )

        // Create a query with a different distance
        val query3 = SpatialBooleanQuery(
            latAttribute = "lat",
            lonAttribute = "lon",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(8.5417 47.3769)"),
            distance = Value.Double(500.0)
        )

        // Create a query with a different reference point
        val query4 = SpatialBooleanQuery(
            latAttribute = "lat",
            lonAttribute = "lon",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(0 0)"),
            distance = Value.Double(1000.0)
        )

        assertEquals(query1, query2, "Identical queries should be equal.")
        assertNotEquals(query1, query3, "Queries with different distances should not be equal.")
        assertNotEquals(query1, query4, "Queries with different reference points should not be equal.")
    }

    /**
     * Test the default limit value of SpatialBooleanQuery.
     */
    @Test
    fun testDefaultLimit() {
        val query = SpatialBooleanQuery(
            latAttribute = "latitude",
            lonAttribute = "longitude",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(8.5417 47.3769)"),
            distance = Value.Double(1000.0)
        )

        assertEquals(Long.MAX_VALUE, query.limit, "Limit should default to Long.MAX_VALUE.")
    }

    /**
     * Test the creation of a SpatialBooleanQuery with a custom SRID.
     */
    @Test
    fun testCustomSrid() {
        val query = SpatialBooleanQuery(
            latAttribute = "latitude",
            lonAttribute = "longitude",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(950920 6005500)", 3857), // Custom SRID (Web Mercator)
            distance = Value.Double(1000.0)
        )

        assertEquals(3857, query.reference.srid, "Custom SRID should be correctly set.")
    }


    /**
     * Verifies that creating a query with BOTH native and struct attributes throws an exception.
     */
    @Test
    fun testThrowsExceptionWhenBothAttributeTypesProvided() {
        val exception = assertThrows<IllegalArgumentException> {
            // Reference the query to ensure init block is triggered
            SpatialBooleanQuery(
                attribute = "location_geography",
                latAttribute = "lat",
                lonAttribute = "lon",
                operator = SpatialOperator.DWITHIN,
                reference = Value.GeographyValue("POINT(0 0)"),
                distance = Value.Double(100.0)
            ).toString() // Force object usage
        }

        assertTrue(exception.message?.contains("either a single 'attribute'") == true)
    }

    /**
     * Verifies that creating a query with NEITHER native nor struct attributes throws an exception.
     */
    @Test
    fun testThrowsExceptionWhenNoAttributesProvided() {
        assertThrows<IllegalArgumentException> {
            SpatialBooleanQuery(
                operator = SpatialOperator.DWITHIN,
                reference = Value.GeographyValue("POINT(0 0)"),
                distance = Value.Double(100.0)
            )
        }
    }

    /**
     * Verifies that creating a query with an INCOMPLETE struct (only lat) throws an exception.
     */
    @Test
    fun testThrowsExceptionWhenStructAttributesIncomplete() {
        assertThrows<IllegalArgumentException> {
            SpatialBooleanQuery(
                latAttribute = "lat",
                operator = SpatialOperator.DWITHIN,
                reference = Value.GeographyValue("POINT(0 0)"),
                distance = Value.Double(100.0)
            )
        }
    }
}