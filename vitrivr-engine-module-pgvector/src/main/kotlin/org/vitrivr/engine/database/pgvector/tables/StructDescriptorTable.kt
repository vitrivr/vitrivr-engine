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
import org.vitrivr.engine.core.model.query.bool.BooleanQuery
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
        is BooleanQuery -> this.parse(query)
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

    /**
     * Centralized method to parse any BooleanQuery into a full Exposed Query.
     */
    private fun parse(query: BooleanQuery): Query {
        val operation = buildExposedOp(query)
        return this.selectAll().where { operation }
            .limit(query.limit.let { if (it == Long.MAX_VALUE) Int.MAX_VALUE else it.toInt() })
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


    /**
     * Recursive helper to build an Exposed Op<Boolean> from any BooleanQuery.
     */
    @Suppress("UNCHECKED_CAST")
    private fun buildExposedOp(query: BooleanQuery): Op<Boolean> {
        return when (query) {
            is SimpleBooleanQuery<*> -> {
                // Logic for simple attribute = value queries
                require(query.attributeName != null) { "Attribute name of simple boolean query must not be null!" }
                val rawQueryValue = query.value.value ?: throw IllegalArgumentException("Attribute value cannot be null.")
                val columnToQuery = this.valueColumns.find { it.name == query.attributeName } as? Column<Any>
                    ?: throw IllegalArgumentException("Attribute '${query.attributeName}' not found in table.")

                when (query.comparison) {
                    ComparisonOperator.EQ -> EqOp(columnToQuery, QueryParameter(rawQueryValue, columnToQuery.columnType))
                    ComparisonOperator.NEQ -> NeqOp(columnToQuery, QueryParameter(rawQueryValue, columnToQuery.columnType))
                    ComparisonOperator.LE -> LessOp(columnToQuery, QueryParameter(rawQueryValue, columnToQuery.columnType))
                    ComparisonOperator.GR -> GreaterOp(columnToQuery, QueryParameter(rawQueryValue, columnToQuery.columnType))
                    ComparisonOperator.LEQ -> LessEqOp(columnToQuery, QueryParameter(rawQueryValue, columnToQuery.columnType))
                    ComparisonOperator.GEQ -> GreaterEqOp(columnToQuery, QueryParameter(rawQueryValue, columnToQuery.columnType))
                    ComparisonOperator.LIKE -> LikeEscapeOp(columnToQuery as Column<String?>, QueryParameter(rawQueryValue as String, columnToQuery.columnType as IColumnType<String>), true, null)
                }
            }
            is CompoundBooleanQuery -> {
                // Logic for AND queries, recursively calls this helper
                if (query.queries.isEmpty()) return Op.TRUE
                query.queries.map { buildExposedOp(it) }.reduce { acc, op -> acc.and(op) }
            }
            is SpatialBooleanQuery -> {
                // Logic for spatial queries, handling both native and struct types
                val fullWktString = "SRID=${query.reference.srid};${query.reference.wkt}"
                val referenceGeogExpression = CustomFunction<String?>("ST_GeogFromText", GeographyColumnType(), stringParam(fullWktString))
                val geographyExpression = if (query.attribute != null) {
                    this.valueColumns.find { it.name == query.attribute } as? Column<String>
                        ?: throw IllegalArgumentException("Geography attribute '${query.attribute}' not found.")
                } else {
                    val latColumn = this.valueColumns.find { it.name == query.latAttribute!! } as? Column<Double>
                        ?: throw IllegalArgumentException("Latitude attribute '${query.latAttribute}' not found.")
                    val lonColumn = this.valueColumns.find { it.name == query.lonAttribute!! } as? Column<Double>
                        ?: throw IllegalArgumentException("Longitude attribute '${query.lonAttribute}' not found.")
                    GeographyFromDoubles(lonColumn, latColumn)
                }

                when (query.operator) {
                    SpatialOperator.DWITHIN -> {
                        val distance = query.distance?.value ?: throw IllegalArgumentException("Distance is required for DWITHIN.")
                        CustomFunction<Boolean>("ST_DWithin", BooleanColumnType(), geographyExpression, referenceGeogExpression, doubleParam(distance), booleanParam(query.useSpheroid?.value ?: true)) eq true
                    }
                    SpatialOperator.INTERSECTS -> CustomFunction<Boolean>("ST_Intersects", BooleanColumnType(), geographyExpression, referenceGeogExpression) eq true
                    SpatialOperator.CONTAINS -> CustomFunction<Boolean>("ST_Contains", BooleanColumnType(), geographyExpression, referenceGeogExpression) eq true
                    SpatialOperator.WITHIN -> CustomFunction<Boolean>("ST_Within", BooleanColumnType(), geographyExpression, referenceGeogExpression) eq true
                    SpatialOperator.EQUALS -> CustomFunction<Boolean>("ST_Equals", BooleanColumnType(), geographyExpression, referenceGeogExpression) eq true
                }
            }
            else -> throw UnsupportedOperationException("Unsupported BooleanQuery subtype in buildExposedOp: ${query::class.simpleName}")
        }
    }


    class GeographyFromDoubles(
        private val lon: Expression<Double>,
        private val lat: Expression<Double>
    ) : Expression<String>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            queryBuilder.append("(ST_MakePoint(")
            lon.toQueryBuilder(queryBuilder)
            queryBuilder.append(", ")
            lat.toQueryBuilder(queryBuilder)
            queryBuilder.append("))::geography")
        }
    }

}
