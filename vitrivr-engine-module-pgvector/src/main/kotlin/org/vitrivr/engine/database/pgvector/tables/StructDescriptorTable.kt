package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.exposed.functions.plainToTsQuery
import org.vitrivr.engine.database.pgvector.exposed.ops.tsMatches
import java.util.*
import org.jetbrains.exposed.sql.javatime.JavaLocalDateTimeColumnType
import java.time.LocalDateTime
import kotlin.reflect.full.primaryConstructor
import org.vitrivr.engine.database.pgvector.exposed.types.geography
import org.vitrivr.engine.core.model.query.bool.CompoundBooleanQuery
import org.vitrivr.engine.core.model.query.bool.SpatialBooleanQuery
import org.vitrivr.engine.core.model.query.spatiotemporal.SpatialOperator
import org.vitrivr.engine.database.pgvector.exposed.types.GeographyColumnType

/**
 * An [AbstractDescriptorTable] for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
class StructDescriptorTable<D: StructDescriptor<*>>(field: Schema.Field<*, D> ): AbstractDescriptorTable<D>(field) {

    /** List of value columns for this [StructDescriptorTable]*/
    private val valueColumns = mutableListOf<Column<*>>()

    init {
        /* Initializes the columns for this [StructDescriptorTable]. */
        for (attribute in this.prototype.layout()) {
            val column = when (val type = attribute.type) {
                Type.Boolean -> bool(attribute.name)
                Type.Byte -> byte(attribute.name)
                Type.Datetime -> datetime(attribute.name)
                Type.Double -> double(attribute.name)
                Type.Float -> float(attribute.name)
                Type.Int -> integer(attribute.name)
                Type.Long -> long(attribute.name)
                Type.Short -> short(attribute.name)
                Type.String -> varchar(attribute.name, 255)
                Type.Text -> text(attribute.name)
                Type.UUID -> uuid(attribute.name)
                Type.Geography -> geography(
                    name = attribute.name,
                    srid = 4326,
                    columnDefinitionInDb = "GEOGRAPHY(POINT, 4326)"
                )
                is Type.FloatVector -> floatVector(attribute.name, type.dimensions)
                else -> error("Unsupported type $type for attribute ${attribute.name} in ${this.tableName}")
            }.let { column ->
                if (attribute.nullable) {
                    column.nullable()
                }
                column
            }
            this.valueColumns.add(column)
        }

        /* Initializes the indexes. */
        this.initializeIndexes()
    }


    /**
     * Converts a [ResultRow] to a [Descriptor].
     *
     * @param row The [ResultRow] to convert.
     * @return The [Descriptor] represented by the [ResultRow].
     */
    override fun rowToDescriptor(row: ResultRow): D {
        /* Obtain constructor. */
        val constructor = this.field.analyser.descriptorClass.primaryConstructor ?: throw IllegalStateException("Provided type ${this.field.analyser.descriptorClass} does not have a primary constructor.")
        val values = mutableMapOf<String, Value<*>?>()
        val parameters: MutableList<Any?> = mutableListOf(
            row[this.id].value,
            row[this.retrievableId].value,
            values,
            this.field
        )

        /* Add the values. */
        for ((column, attribute) in this.valueColumns.zip(this.prototype.layout())) {
            val value = row[column]
            if (value == null) {
                values[attribute.name] = null
            } else {
                values[attribute.name] = when (attribute.type) {
                    Type.Boolean -> Value.Boolean(value as Boolean)
                    Type.Byte -> Value.Byte(value as Byte)
                    Type.Datetime -> Value.DateTime(value as LocalDateTime)
                    Type.Double -> Value.Double(value as Double)
                    Type.Float -> Value.Float(value as Float)
                    Type.Int -> Value.Int(value as Int)
                    Type.Long -> Value.Long(value as Long)
                    Type.Short -> Value.Short(value as Short)
                    Type.String -> Value.String(value as String)
                    Type.Text -> Value.Text(value as String)
                    Type.UUID -> Value.UUIDValue(value as UUID)
                    Type.Geography -> Value.GeographyValue(value as String)
                    is Type.BooleanVector -> Value.BooleanVector(value as BooleanArray)
                    is Type.DoubleVector -> Value.DoubleVector(value as DoubleArray)
                    is Type.FloatVector -> Value.FloatVector(value as FloatArray)
                    is Type.LongVector -> Value.LongVector(value as LongArray)
                    is Type.IntVector -> Value.IntVector(value as IntArray)
                }
            }
        }

        /* Call constructor. */
        return constructor.call(*parameters.toTypedArray())
    }

    /**
     * Converts a [org.vitrivr.engine.core.model.query.Query] into a [Query] that can be executed against the database.
     *
     * @param query The [org.vitrivr.engine.core.model.query.Query] to convert.
     * @return The [Query] that can be executed against the database.
     * @throws UnsupportedOperationException If the query is not supported.
     */
    override fun parse(query: org.vitrivr.engine.core.model.query.Query): Query = when(query) {
        is SimpleFulltextQuery -> this.parse(query)
        is SimpleBooleanQuery<*> -> this.parse(query)
        is CompoundBooleanQuery -> this.parse(query)
        is SpatialBooleanQuery -> this.parse(query)
        else -> throw UnsupportedOperationException("Unsupported query type: ${query::class.simpleName}")
    }

    /**
     * Converts a [SimpleFulltextQuery] into a [Query] that can be executed against the database.
     *
     * @param query [SimpleFulltextQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    @Suppress("UNCHECKED_CAST")
    private fun parse(query: SimpleFulltextQuery): Query = this.selectAll().where {
        require(query.attributeName != null) { "Attribute name of boolean query must not be null!" }
        val value = query.value.value as? String ?: throw IllegalArgumentException("Attribute value of fulltext query must be a string")
        val descriptor = this@StructDescriptorTable.valueColumns.find { it.name == query.attributeName } as Column<String>
        descriptor tsMatches plainToTsQuery(stringParam(value))
    }

    /**TODO ADD further comparison operators for geography type
     * Converts a [SimpleBooleanQuery] into a [Query] that can be executed against the database.
     *
     * @param query The [SimpleBooleanQuery] to convert.
     * @return The [Query] that can be executed against the database.
     */
    @Suppress("UNCHECKED_CAST")
    private fun parse(query: SimpleBooleanQuery<*>): Query = this.selectAll().where {
        require(query.attributeName != null) { "Attribute name of boolean query must not be null!" }
        val value = query.value.value ?: throw IllegalArgumentException("Attribute value of boolean query must not be null")
        val descriptor = this@StructDescriptorTable.valueColumns.find { it.name == query.attributeName } as Column<Any>
        when(query.comparison) {
            ComparisonOperator.EQ -> EqOp(descriptor, QueryParameter(value, descriptor.columnType))
            ComparisonOperator.NEQ -> NeqOp(descriptor, QueryParameter(value, descriptor.columnType))
            ComparisonOperator.LE -> LessOp(descriptor, QueryParameter(value, descriptor.columnType))
            ComparisonOperator.GR -> GreaterOp(descriptor, QueryParameter(value, descriptor.columnType))
            ComparisonOperator.LEQ -> LessEqOp(descriptor, QueryParameter(value, descriptor.columnType))
            ComparisonOperator.GEQ -> GreaterEqOp(descriptor, QueryParameter(value, descriptor.columnType))
            ComparisonOperator.LIKE -> LikeEscapeOp(descriptor, QueryParameter(value, descriptor.columnType), true, null)
            else -> throw IllegalArgumentException("Unsupported comparison type: ${query.comparison}")
        }
    }



    @Suppress("UNCHECKED_CAST")
    private fun parse(query: CompoundBooleanQuery): Query {
        // Local function to convert a SimpleBooleanQuery clause to an Exposed Op<Boolean>
        fun clauseToOp(clause: SimpleBooleanQuery<*>): Op<Boolean> {
            require(clause.attributeName != null) { "Attribute name of a boolean query clause must not be null!" }
            val value = clause.value.value ?: throw IllegalArgumentException("Attribute value of boolean query clause must not be null")
            val descriptor = this.valueColumns.find { it.name == clause.attributeName } as Column<Any>
            return when(clause.comparison) {
                ComparisonOperator.EQ -> EqOp(descriptor, QueryParameter(value, descriptor.columnType))
                ComparisonOperator.NEQ -> NeqOp(descriptor, QueryParameter(value, descriptor.columnType))
                ComparisonOperator.LE -> LessOp(descriptor, QueryParameter(value, descriptor.columnType))
                ComparisonOperator.GR -> GreaterOp(descriptor, QueryParameter(value, descriptor.columnType))
                ComparisonOperator.LEQ -> LessEqOp(descriptor, QueryParameter(value, descriptor.columnType))
                ComparisonOperator.GEQ -> GreaterEqOp(descriptor, QueryParameter(value, descriptor.columnType))
                ComparisonOperator.LIKE -> LikeEscapeOp(descriptor, QueryParameter(value, descriptor.columnType), true, null)
            }
        }

        // Map each clause to an Op<Boolean>.
        // This version assumes no nested compound queries.
        val exposedConditions = query.queries.map { clause ->
            when (clause) {
                is SimpleBooleanQuery<*> -> clauseToOp(clause)
                else -> throw IllegalArgumentException("CompoundBooleanQuery can only contain SimpleBooleanQuery clauses in this implementation.")
            }
        }

        // Combine all conditions with AND. The init block of CompoundBooleanQuery ensures the list is not empty.
        val finalCondition = exposedConditions.reduce { acc, op -> acc.and(op) }

        // Construct and return the final Exposed Query
        return this.selectAll().where { finalCondition }
            .limit(query.limit.let { if (it == Long.MAX_VALUE) Int.MAX_VALUE else it.toInt() })
    }



    /**
     * Converts a [SpatialBooleanQuery] into an Exposed [Query] that can be executed against the database.
     * TODO Support native GEOGRAPHY type in the future
     */
    @Suppress("UNCHECKED_CAST")
    private fun parse(query: SpatialBooleanQuery): Query {
        // Find the actual latitude and longitude columns from this table's definition
        val latColumn = this.valueColumns.find { it.name == query.latAttribute } as? Column<Double>
            ?: throw IllegalArgumentException("Latitude attribute '${query.latAttribute}' not found or not a Double column in table ${this.tableName}.")

        val lonColumn = this.valueColumns.find { it.name == query.lonAttribute } as? Column<Double>
            ?: throw IllegalArgumentException("Longitude attribute '${query.lonAttribute}' not found or not a Double column in table ${this.tableName}.")

        // Create the geography object on-the-fly from the two Double columns
        val geographyFromColumns = GeographyFromDoubles(lonColumn, latColumn)

        // Combine the SRID and WKT into a single string for ST_GeogFromText
        val fullWktString = "SRID=${query.reference.srid};${query.reference.wkt}"

        // Create the reference geography from the query input using the combined string
        val referenceGeogExpression = CustomFunction<String?>(
            "ST_GeogFromText",
            GeographyColumnType(),
            stringParam(fullWktString) // Pass the single, combined string
        )

        // Build the PostGIS function call (e.g., ST_DWithin)
        val op = when (query.operator) {
            SpatialOperator.DWITHIN -> {
                val distanceMeters = query.distance?.value
                    ?: throw IllegalArgumentException("Distance (radius) is required for DWITHIN operator.")
                val useSpheroid = query.useSpheroid?.value ?: true

                CustomFunction<Boolean>(
                    "ST_DWithin",
                    BooleanColumnType(),
                    geographyFromColumns,
                    referenceGeogExpression,
                    doubleParam(distanceMeters),
                    booleanParam(useSpheroid)
                ) eq true // Compare the boolean result of the function with 'true' to make it an Op<Boolean>
            }
            else -> throw UnsupportedOperationException("Spatial operator '${query.operator}' is not yet supported for this query path.")
        }

        return this.selectAll().where { op }
    }


    /**
     * Sets the value of the descriptor in the [InsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    @Suppress("UNCHECKED_CAST")
    override fun InsertStatement<*>.setValue(d: D) {
        val values = d.values()
        for (columnInTable in this@StructDescriptorTable.valueColumns) {
            val rawValueFromDescriptor = values[columnInTable.name]?.value
            val targetColumn = columnInTable as Column<Any?>

            this[targetColumn] = if (rawValueFromDescriptor == null) {
                null
            } else {
                // If the column is defined as a LocalDateTime type in Exposed
                if (targetColumn.columnType is JavaLocalDateTimeColumnType) {
                    // We now strictly expect rawValueFromDescriptor to be LocalDateTime
                    if (rawValueFromDescriptor is LocalDateTime) {
                        rawValueFromDescriptor
                    } else {
                        // We face inconsistency: the Value system or descriptor provided a non-LocalDateTime for a Datetime attribute.
                        throw IllegalStateException(
                            "Type mismatch for datetime column '${targetColumn.name}'. " +
                                    "Expected LocalDateTime from descriptor, but got ${rawValueFromDescriptor::class.simpleName}. " +
                                    "This indicates an issue with descriptor creation or the Value system."
                        )
                    }
                } else {
                    // For all other column types, pass the raw value.
                    rawValueFromDescriptor
                }
            }
        }
    }

    /**
     * Sets the value of the descriptor in the [BatchInsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    @Suppress("UNCHECKED_CAST")
    override fun BatchInsertStatement.setValue(d: D) {
        val values = d.values()
        for (columnInTable in this@StructDescriptorTable.valueColumns) {
            val rawValueFromDescriptor = values[columnInTable.name]?.value
            val targetColumn = columnInTable as Column<Any?>

            this[targetColumn] = if (rawValueFromDescriptor == null) {
                null
            } else {
                if (targetColumn.columnType is JavaLocalDateTimeColumnType) {
                    if (rawValueFromDescriptor is LocalDateTime) {
                        rawValueFromDescriptor
                    } else {
                        throw IllegalStateException(
                            "Type mismatch for datetime column '${targetColumn.name}'. " +
                                    "Expected LocalDateTime, got ${rawValueFromDescriptor::class.simpleName}."
                        )
                    }
                } else {
                    rawValueFromDescriptor
                }
            }
        }
    }

    /**
     * Sets the value of the descriptor in the [UpdateStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    @Suppress("UNCHECKED_CAST")
    override fun UpdateStatement.setValue(d: D) {
        val values = d.values()
        for (columnInTable in this@StructDescriptorTable.valueColumns) {
            val rawValueFromDescriptor = values[columnInTable.name]?.value
            val targetColumn = columnInTable as Column<Any?>

            this[targetColumn] = if (rawValueFromDescriptor == null) {
                null
            } else {
                if (targetColumn.columnType is JavaLocalDateTimeColumnType) {
                    if (rawValueFromDescriptor is LocalDateTime) {
                        rawValueFromDescriptor
                    } else {
                        throw IllegalStateException(
                            "Type mismatch for datetime column '${targetColumn.name}'. " +
                                    "Expected LocalDateTime, got ${rawValueFromDescriptor::class.simpleName}."
                        )
                    }
                } else {
                    rawValueFromDescriptor
                }
            }
        }
    }

    class GeographyFromDoubles(
        private val lon: Expression<Double>,
        private val lat: Expression<Double>
    ) : Expression<String>() { // Treat as String for Exposed's GeographyColumnType
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            queryBuilder.append("(ST_MakePoint(")
            lon.toQueryBuilder(queryBuilder)
            queryBuilder.append(", ")
            lat.toQueryBuilder(queryBuilder)
            queryBuilder.append("))::geography")
        }
    }

}
