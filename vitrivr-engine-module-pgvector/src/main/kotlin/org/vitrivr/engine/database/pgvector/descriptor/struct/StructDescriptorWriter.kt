package org.vitrivr.engine.database.pgvector.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.LOGGER
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorWriter
import org.vitrivr.engine.database.pgvector.descriptor.model.PgBitVector
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import java.sql.Date
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.SQLType
import java.util.*

/**
 * An [AbstractDescriptorWriter] for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StructDescriptorWriter(field: Schema.Field<*, StructDescriptor>, connection: PgVectorConnection) : AbstractDescriptorWriter<StructDescriptor>(field, connection) {

    /**
     * Adds (writes) a single [StructDescriptor] using this [StructDescriptorWriter].
     *
     * @param item The [StructDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: StructDescriptor): Boolean {
        try {
            this.prepareInsertStatement().use { stmt ->
                stmt.setObject(1, item.id)
                stmt.setObject(2, item.retrievableId)
                var i = 3
                for (attribute in item.schema()) {
                    val value = item.values()[attribute.name]
                    if (value != null) {
                        stmt.setValue(i++, value)
                    } else {
                        stmt.setNull(i++, attribute.type.toSqlType())
                    }
                }
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to INSERT descriptor ${item.id} into '$tableName' due to SQL error." }
            return false
        }
    }

    /**
     * Adds (writes) a batch of [StructDescriptor] using this [StructDescriptorWriter].
     *
     * @param items A [Iterable] of [StructDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<StructDescriptor>): Boolean {
        try {
            this.prepareInsertStatement().use { stmt ->
                for (item in items) {
                    stmt.setObject(1, item.id)
                    stmt.setObject(2, item.retrievableId)
                    var i = 3
                    for (attribute in item.schema()) {
                        val value = item.values()[attribute.name]
                        if (value != null) {
                            stmt.setValue(i++, value)
                        } else {
                            stmt.setNull(i++, attribute.type.toSqlType())
                        }
                    }
                    stmt.addBatch()
                }
                return stmt.executeBatch().all { it == 1 }
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to INSERT descriptors into '$tableName' due to SQL error." }
            return false
        }
    }

    /**
     * Updates a specific [StructDescriptor] using this [StructDescriptorWriter].
     *
     * @param item A [StructDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: StructDescriptor): Boolean {
        try {
            this.prepareUpdateStatement().use { stmt ->
                stmt.setObject(1, item.retrievableId)
                var i = 2
                for (attribute in item.schema()) {
                    val value = item.values()[attribute.name]
                    if (value != null) {
                        stmt.setValue(i++, value)
                    } else {
                        stmt.setNull(i++, attribute.type.toSqlType())
                    }
                }
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to UPDATE descriptors in '$tableName' due to SQL error." }
            return false
        }
    }

    /**
     * Sets a value of [Value] type in a [PreparedStatement].
     */
    private fun PreparedStatement.setValue(index: Int, value: Value<*>) = when (value) {
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
    private fun Type.toSqlType(): Int = when (this) {
        Type.Boolean -> JDBCType.BOOLEAN
        Type.Byte -> JDBCType.TINYINT
        Type.Short -> JDBCType.SMALLINT
        Type.Int -> JDBCType.INTEGER
        Type.Long -> JDBCType.BIGINT
        Type.Float -> JDBCType.REAL
        Type.Double -> JDBCType.DOUBLE
        Type.Datetime -> JDBCType.DATE
        Type.String -> JDBCType.VARCHAR
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
    private fun prepareUpdateStatement(): PreparedStatement {
        val statement = StringBuilder("UPDATE $tableName SET $RETRIEVABLE_ID_COLUMN_NAME = ?")
        for (field in this.field.analyser.prototype(this.field).schema()) {
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
    private fun prepareInsertStatement(): PreparedStatement {
        val statement = StringBuilder("INSERT INTO $tableName ($DESCRIPTOR_ID_COLUMN_NAME, $RETRIEVABLE_ID_COLUMN_NAME")
        for (field in this.field.analyser.prototype(this.field).schema()) {
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