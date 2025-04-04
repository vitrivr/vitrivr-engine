package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.sql.*
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
import kotlin.reflect.full.primaryConstructor

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
                    Type.Datetime -> Value.DateTime(value as Date)
                    Type.Double -> Value.Double(value as Double)
                    Type.Float -> Value.Float(value as Float)
                    Type.Int -> Value.Int(value as Int)
                    Type.Long -> Value.Long(value as Long)
                    Type.Short -> Value.Short(value as Short)
                    Type.String -> Value.String(value as String)
                    Type.Text -> Value.Text(value as String)
                    Type.UUID -> Value.UUIDValue(value as UUID)
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

    /**
     * Sets the value of the descriptor in the [InsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    @Suppress("UNCHECKED_CAST")
    override fun InsertStatement<*>.setValue(d: D) {
        val values = d.values()
        for (c in this@StructDescriptorTable.valueColumns) {
            this[c as Column<Any?>] = values[c.name]?.value
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
        for (c in this@StructDescriptorTable.valueColumns) {
            this[c as Column<Any?>] = values[c.name]?.value
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
        for (c in this@StructDescriptorTable.valueColumns) {
            this[c as Column<Any?>] = values[c.name]?.value
        }
    }
}