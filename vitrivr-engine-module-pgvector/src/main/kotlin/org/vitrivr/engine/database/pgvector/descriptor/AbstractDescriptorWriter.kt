package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import org.vitrivr.engine.database.pgvector.descriptor.struct.StructDescriptorWriter
import java.sql.*

/**
 * An abstract implementation of a [DescriptorWriter] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorWriter<D : Descriptor>(final override val field: Schema.Field<*, D>, override val connection: PgVectorConnection): DescriptorWriter<D> {
    /** The name of the table backing this [AbstractDescriptorInitializer]. */
    protected val tableName: String = "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName}"

    /** The [Descriptor] prototype for this [AbstractDescriptorWriter]. */
    protected val prototype = this.field.analyser.prototype(this.field)

    /**
     * Deletes (writes) a [Descriptor] of type [D] using this [AbstractDescriptorWriter].
     *
     * @param item A [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: D): Boolean {
        try {
            this.connection.jdbc.prepareStatement("DELETE FROM $tableName WHERE $DESCRIPTOR_ID_COLUMN_NAME = ?;").use { stmt ->
                stmt.setObject(1, item.id)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) {  "Failed to delete descriptor ${item.id} due to SQL error." }
            return false
        }
    }

    /**
     * Deletes (writes) [Descriptor]s of type [D] using this [AbstractDescriptorWriter].
     *
     * @param items A [Iterable] of [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun deleteAll(items: Iterable<D>): Boolean {
        try {
            this.connection.jdbc.prepareStatement("DELETE FROM $tableName WHERE $DESCRIPTOR_ID_COLUMN_NAME = ANY (?);").use { stmt ->
                val values = items.map { it.id }.toTypedArray()
                stmt.setArray(1, this.connection.jdbc.createArrayOf("uuid", values))
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to delete descriptors due to SQL error." }
            return false
        }
    }

    /**
     * Sets a value of [Value] type in a [PreparedStatement].
     */
    protected fun PreparedStatement.setValue(index: Int, value: Value<*>) = when (value) {
        is Value.Boolean -> this.setBoolean(index, value.value)
        is Value.Byte -> this.setByte(index, value.value)
        is Value.DateTime -> this.setDate(index, Date(value.value.toInstant().toEpochMilli()))
        is Value.Double -> this.setDouble(index, value.value)
        is Value.Float -> this.setFloat(index, value.value)
        is Value.Int -> this.setInt(index, value.value)
        is Value.Long -> this.setLong(index, value.value)
        is Value.Short -> this.setShort(index, value.value)
        is Value.String -> this.setString(index, value.value)
        is Value.BooleanVector -> this.setObject(index, PgBitVector(value.value))
        is Value.FloatVector -> this.setObject(index, PgVector(value.value))
        else -> throw IllegalArgumentException("Unsupported value type for vector value.")
    }

    /**
     * Converts a [Type] to a [SQLType].
     */
    protected fun Type.toSqlType(): Int = when (this) {
        Type.Boolean -> JDBCType.BOOLEAN
        Type.Byte -> JDBCType.TINYINT
        Type.Short -> JDBCType.SMALLINT
        Type.Int -> JDBCType.INTEGER
        Type.Long -> JDBCType.BIGINT
        Type.Float -> JDBCType.REAL
        Type.Double -> JDBCType.DOUBLE
        Type.Datetime -> JDBCType.DATE
        Type.String -> JDBCType.VARCHAR
        Type.Text -> JDBCType.CLOB
        is Type.BooleanVector -> JDBCType.ARRAY
        is Type.DoubleVector -> JDBCType.ARRAY
        is Type.FloatVector -> JDBCType.ARRAY
        is Type.IntVector -> JDBCType.ARRAY
        is Type.LongVector -> JDBCType.ARRAY
    }.ordinal

    /**
     * Prepares an INSERT statement for this [StructDescriptorWriter].
     *
     * @return [PreparedStatement]
     */
    protected fun prepareUpdateStatement(): PreparedStatement {
        val statement = StringBuilder("UPDATE $tableName SET $RETRIEVABLE_ID_COLUMN_NAME = ?")
        for (field in this.prototype.schema()) {
            statement.append(", \"${field.name}\" = ?")
        }
        statement.append("WHERE $DESCRIPTOR_ID_COLUMN_NAME = ?;")
        return this.connection.jdbc.prepareStatement(statement.toString())
    }

    /**
     * Prepares an INSERT statement for this [StructDescriptorWriter].
     *
     * @return [PreparedStatement]
     */
    protected fun prepareInsertStatement(): PreparedStatement {
        val statement = StringBuilder("INSERT INTO $tableName ($DESCRIPTOR_ID_COLUMN_NAME, $RETRIEVABLE_ID_COLUMN_NAME")
        for (field in this.prototype.schema()) {
            statement.append(", \"${field.name}\"")
        }
        statement.append(") VALUES (?, ?")
        for (field in this.field.analyser.prototype(this.field).schema()) {
            statement.append(", ?")
        }
        statement.append(");")
        return this.connection.jdbc.prepareStatement(statement.toString())
    }
}