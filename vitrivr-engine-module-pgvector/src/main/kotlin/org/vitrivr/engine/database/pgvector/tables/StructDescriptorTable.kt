package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*
import kotlin.reflect.full.primaryConstructor

/**
 * An [AbstractDescriptorTable] for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructDescriptorTable<D: StructDescriptor<*>>(field: Schema.Field<*, D> ): AbstractDescriptorTable<D>(field) {

    /** List of value columns for this [StructDescriptorTable]*/
    private val valueColumns = mutableListOf<Column<*>>()

    /* Initializes the columns for this [StructDescriptorTable]. */
    init {
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
                is Type.BooleanVector -> TODO()
                is Type.DoubleVector -> TODO()
                is Type.IntVector -> TODO()
                is Type.LongVector -> TODO()
            }.index().let { column ->
                if (attribute.nullable) {
                    column.nullable()
                }
                column
            }
            this.valueColumns.add(column)
        }
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
        val parameters: MutableList<Any?> = mutableListOf(
            row[this.id],
            row[this.retrievableId]
        )

        /* Add the values. */
        for ((column, attribute) in this.valueColumns.zip(this.prototype.layout())) {
            val value = when (attribute.type) {
                Type.Boolean -> Value.Boolean(row[column] as Boolean)
                Type.Byte -> Value.Byte(row[column] as Byte)
                Type.Datetime -> Value.DateTime(row[column] as Date)
                Type.Double -> Value.Double(row[column] as Double)
                Type.Float -> Value.Float(row[column] as Float)
                Type.Int -> Value.Int(row[column] as Int)
                Type.Long -> Value.Long(row[column] as Long)
                Type.Short -> Value.Short(row[column] as Short)
                Type.String -> Value.String(row[column] as String)
                Type.Text -> Value.String(row[column] as String)
                Type.UUID -> Value.UUIDValue(row[column] as UUID)
                is Type.BooleanVector -> Value.BooleanVector(row[column] as BooleanArray)
                is Type.DoubleVector -> Value.DoubleVector(row[column] as DoubleArray)
                is Type.FloatVector -> Value.FloatVector(row[column] as FloatArray)
                is Type.LongVector -> Value.LongVector(row[column] as LongArray)
                is Type.IntVector -> Value.IntVector(row[column] as IntArray)
            }
        }

        /* Add the field. */
        parameters.add(this.field)

        /* Call constructor. */
        return constructor.call(*parameters.toTypedArray())
    }

    /**
     * Sets the value of the descriptor in the [InsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
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
    override fun UpdateStatement.setValue(d: D) {
        val values = d.values()
        for (c in this@StructDescriptorTable.valueColumns) {
            this[c as Column<Any?>] = values[c.name]?.value
        }
    }
}