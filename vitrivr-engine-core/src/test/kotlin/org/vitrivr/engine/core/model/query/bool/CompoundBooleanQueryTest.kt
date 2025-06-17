package org.vitrivr.engine.core.model.query.bool

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.spatiotemporal.SpatialOperator
import org.vitrivr.engine.core.model.types.Value
import java.time.format.DateTimeFormatter


/**
 * Extension function to convert a vitrivr-engine ComparisonOperator to a SQL operator string.
 */
private fun ComparisonOperator.toSqlOperator(): String = when (this) {
    ComparisonOperator.EQ -> "="
    ComparisonOperator.NEQ -> "!="
    ComparisonOperator.LE -> "<"
    ComparisonOperator.GR -> ">"
    ComparisonOperator.LEQ -> "<="
    ComparisonOperator.GEQ -> ">="
    ComparisonOperator.LIKE -> "LIKE"
}

/**
 * Extension function to convert a vitrivr-engine Value to a SQL literal string.
 * It correctly adds single quotes for string and date types.
 */
private fun Value<*>.toSqlString(): String = when (this) {
    is Value.String, is Value.Text -> "'${this.value.replace("'", "''")}'"
    is Value.DateTime -> "'${this.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}'"
    is Value.Boolean -> this.value.toString().uppercase()
    is Value.UUIDValue -> "'${this.value}'"
    else -> this.value.toString()
}

/**
 * The main extension function. Recursively generates a SQL WHERE clause string from any BooleanQuery.
 */
private fun BooleanQuery.toSQL(): String {
    return when (this) {
        is SimpleBooleanQuery<*> -> {
            // Handles a single condition like "age >= 21"
            val attr = this.attributeName ?: "field"
            val op = this.comparison.toSqlOperator()
            val valueStr = this.value.toSqlString()
            "$attr $op $valueStr"
        }
        is CompoundBooleanQuery -> {
            // Handles an AND query by recursively calling toSQL() on each part and joining them with "AND".
            if (this.queries.isEmpty()) {
                return "TRUE"
            }
            this.queries.joinToString(" AND ") { "(${it.toSQL()})" }
        }
        is SpatialBooleanQuery -> {
            // a simple string representation for testing purposes
            "SPATIAL ${this.operator} on " +
                    (if(this.attribute != null) "'${this.attribute}'" else "'${this.latAttribute}/${this.lonAttribute}'") +
                    " near '${this.reference.wkt}'"
        }
        else -> throw UnsupportedOperationException("SQL generation not supported for ${this::class.simpleName}")
    }
}



/**
 * Test class for [CompoundBooleanQuery] to verify its functionality.
 *
 * @author henrikluemkemann
 * @version 1.0.0
 */
class CompoundBooleanQueryTest {

    /**
     * Test the creation of a CompoundBooleanQuery with multiple BooleanQuery instances.
     */
    @Test
    fun testCreation() {
        // Create simple boolean queries
        val query1 = SimpleBooleanQuery(
            value = Value.Text("test"),
            comparison = ComparisonOperator.EQ,
            attributeName = "attribute1",
            limit = 10
        )
        
        val query2 = SimpleBooleanQuery(
            value = Value.Int(42),
            comparison = ComparisonOperator.GR,
            attributeName = "attribute2",
            limit = 20
        )
        
        // Create a compound query with the simple queries
        val compoundQuery = CompoundBooleanQuery(
            queries = listOf(query1, query2),
            limit = 5
        )
        
        // Verify the compound query properties
        assertEquals(listOf(query1, query2), compoundQuery.queries)
        assertEquals(5, compoundQuery.limit)
    }
    
    /**
     * Test the equality of CompoundBooleanQuery instances.
     */
    @Test
    fun testEquality() {
        // Create simple boolean queries
        val query1 = SimpleBooleanQuery(
            value = Value.Text("test"),
            comparison = ComparisonOperator.EQ,
            attributeName = "attribute1",
            limit = 10
        )
        
        val query2 = SimpleBooleanQuery(
            value = Value.Int(42),
            comparison = ComparisonOperator.GR,
            attributeName = "attribute2",
            limit = 20
        )
        
        // Create identical compound queries
        val compoundQuery1 = CompoundBooleanQuery(
            queries = listOf(query1, query2),
            limit = 5
        )
        
        val compoundQuery2 = CompoundBooleanQuery(
            queries = listOf(query1, query2),
            limit = 5
        )
        
        // Create a different compound query
        val compoundQuery3 = CompoundBooleanQuery(
            queries = listOf(query1),
            limit = 5
        )
        
        val compoundQuery4 = CompoundBooleanQuery(
            queries = listOf(query1, query2),
            limit = 10
        )
        
        // Verify equality
        assertEquals(compoundQuery1, compoundQuery2)
        assertNotEquals(compoundQuery1, compoundQuery3)
        assertNotEquals(compoundQuery1, compoundQuery4)
    }
    
    /**
     * Test the creation of a CompoundBooleanQuery with an empty list of queries.
     */
    @Test
    fun testEmptyQueries() {
        // Create a compound query with an empty list of queries
        val compoundQuery = CompoundBooleanQuery(
            queries = emptyList(),
            limit = 5
        )
        
        // Verify the compound query properties
        assertEquals(emptyList<BooleanQuery>(), compoundQuery.queries)
        assertEquals(5, compoundQuery.limit)
    }
    
    /**
     * Test the creation of a CompoundBooleanQuery with nested CompoundBooleanQuery instances.
     */
    @Test
    fun testNestedCompoundQueries() {
        // Create simple boolean queries
        val query1 = SimpleBooleanQuery(
            value = Value.Text("test"),
            comparison = ComparisonOperator.EQ,
            attributeName = "attribute1",
            limit = 10
        )
        
        val query2 = SimpleBooleanQuery(
            value = Value.Int(42),
            comparison = ComparisonOperator.GR,
            attributeName = "attribute2",
            limit = 20
        )
        
        // Create a nested compound query
        val nestedCompoundQuery = CompoundBooleanQuery(
            queries = listOf(query1, query2),
            limit = 15
        )
        
        // Create another simple query
        val query3 = SimpleBooleanQuery(
            value = Value.Double(3.14),
            comparison = ComparisonOperator.LE,
            attributeName = "attribute3",
            limit = 30
        )
        
        // Create a compound query that includes the nested compound query
        val outerCompoundQuery = CompoundBooleanQuery(
            queries = listOf(nestedCompoundQuery, query3),
            limit = 5
        )
        
        assertEquals(listOf(nestedCompoundQuery, query3), outerCompoundQuery.queries)
        assertEquals(5, outerCompoundQuery.limit)
    }
    
    /**
     * Test the default limit value of CompoundBooleanQuery.
     */
    @Test
    fun testDefaultLimit() {
        val query1 = SimpleBooleanQuery(
            value = Value.Text("test"),
            comparison = ComparisonOperator.EQ,
            attributeName = "attribute1"
        )
        
        val query2 = SimpleBooleanQuery(
            value = Value.Int(42),
            comparison = ComparisonOperator.GR,
            attributeName = "attribute2"
        )
        
        val compoundQuery = CompoundBooleanQuery(
            queries = listOf(query1, query2)
        )
        
        assertEquals(Long.MAX_VALUE, compoundQuery.limit)
    }

    @Test
    fun testSQLGeneration() {
        val query1 = SimpleBooleanQuery(
            value = Value.Text("test"),
            comparison = ComparisonOperator.EQ,
            attributeName = "name"
        )
        val query2 = SimpleBooleanQuery(
            value = Value.Int(10),
            comparison = ComparisonOperator.LEQ,
            attributeName = "age"
        )
        val compoundQuery = CompoundBooleanQuery(listOf(query1, query2))

        val expectedSQL = "(name = 'test') AND (age <= 10)"
        assertEquals(expectedSQL, compoundQuery.toSQL())
    }

    @Test
    fun testNestedSQLGeneration() {
        val query1 = SimpleBooleanQuery(
            value = Value.Text("admin"),
            comparison = ComparisonOperator.EQ,
            attributeName = "role"
        )
        val query2 = SimpleBooleanQuery(
            value = Value.Int(100),
            comparison = ComparisonOperator.GEQ,
            attributeName = "score"
        )
        val innerCompound = CompoundBooleanQuery(listOf(query1, query2))

        val query3 = SimpleBooleanQuery(
            value = Value.Boolean(true),
            comparison = ComparisonOperator.EQ,
            attributeName = "active"
        )

        val outerCompound = CompoundBooleanQuery(listOf(innerCompound, query3))

        val expectedSQL = "((role = 'admin') AND (score >= 100)) AND (active = TRUE)"
        assertEquals(expectedSQL, outerCompound.toSQL())
    }



    @Test
    fun testSQLWithEmptyQueryList() {
        val compoundQuery = CompoundBooleanQuery(emptyList())
        assertEquals("TRUE", compoundQuery.toSQL())
    }

    @Test
    fun testOperatorPrecedence() {
        val q1 = SimpleBooleanQuery(Value.Text("X"), ComparisonOperator.EQ, "attr1")
        val q2 = SimpleBooleanQuery(Value.Int(5), ComparisonOperator.GR, "attr2")
        val q3 = SimpleBooleanQuery(Value.Boolean(true), ComparisonOperator.EQ, "flag")

        val inner = CompoundBooleanQuery(listOf(q1, q2))
        val outer = CompoundBooleanQuery(listOf(inner, q3))

        val expectedSQL = "((attr1 = 'X') AND (attr2 > 5)) AND (flag = TRUE)"
        assertEquals(expectedSQL, outer.toSQL())
    }


    @Test
    fun testSqlGenerationWithSpatialQuery() {

        val simpleQuery = SimpleBooleanQuery(
            value = Value.String("trip"),
            comparison = ComparisonOperator.EQ,
            attributeName = "category"
        )

        val spatialQuery = SpatialBooleanQuery(
            latAttribute = "lat",
            lonAttribute = "lon",
            operator = SpatialOperator.DWITHIN,
            reference = Value.GeographyValue("POINT(8.54 47.37)"),
            distance = Value.Double(500.0)
        )

        val compoundQuery = CompoundBooleanQuery(
            queries = listOf(simpleQuery, spatialQuery)
        )

        val expectedSQL = "((category = 'trip') AND (SPATIAL DWITHIN lat,lon ON 'POINT(8.54 47.37)' dist 500.0))"

        fun BooleanQuery.toSQLWithSpatial(): String {
            return when (this) {
                is SpatialBooleanQuery -> "SPATIAL ${this.operator} ${this.latAttribute},${this.lonAttribute} ON '${this.reference.wkt}' dist ${this.distance?.value}"
                else -> this.toSQL()
            }
        }

        val actualSql = "(" + compoundQuery.queries.joinToString(" AND ") { "(${it.toSQLWithSpatial()})" } + ")"

        assertEquals(expectedSQL, actualSql)
    }

}