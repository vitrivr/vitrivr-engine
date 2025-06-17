package org.vitrivr.engine.database.pgvector.exposed.types

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

class GeographyColumnType(
    private val srid: Int = 4326,
    private val columnDefinitionInDb: String = "GEOGRAPHY"
) : ColumnType<String>(false) { // False: not nullable by default in ColumnType

    override fun sqlType(): String = columnDefinitionInDb

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        if (value == null) {
            stmt.setNull(index, this)
        } else {
            val wktString = value as? String
                ?: error("GeographyColumnType received a non-string value: ${value::class.qualifiedName}. Expected WKT String.")

            val pgObject = PGobject().apply {
                type = "geography"
                this.value = wktString
            }

            stmt[index] = pgObject
        }
    }

    override fun valueFromDB(value: Any): String {
        return when (value) {
            is String -> value
            is PGobject -> {
                if (value.type.equals("geography", ignoreCase = true) || value.type.equals("geometry", ignoreCase = true)) {
                    value.value ?: throw NullPointerException("Geography PGobject received from DB has a null value.")
                } else {
                    throw IllegalArgumentException("Unexpected PGobject type '${value.type}' for GeographyColumnType. Value: '${value.value}'")
                }
            }
            else -> throw IllegalArgumentException("Unexpected value type from DB for GeographyColumnType: ${value::class.java.name}. Value: '$value'")
        }
    }

    /**
     * This method is crucial for how Exposed writes literal values into SQL statements,
     * especially in some batch insert scenarios or DDL defaults.
     */
    override fun nonNullValueToString(value: String): String {
        val escapedWkt = value.replace("'", "''")
        return "ST_GeogFromText('$escapedWkt', $srid)" // Use SRID from the class instance
    }

    override fun valueToString(value: String?): String =
        value?.let { nonNullValueToString(it) } ?: super.valueToString(value)
}

// Table.geography(...) extension function remains the same
fun Table.geography(
    name: String,
    srid: Int = 4326,
    columnDefinitionInDb: String = "GEOGRAPHY"
): Column<String> = registerColumn(name, GeographyColumnType(srid, columnDefinitionInDb))