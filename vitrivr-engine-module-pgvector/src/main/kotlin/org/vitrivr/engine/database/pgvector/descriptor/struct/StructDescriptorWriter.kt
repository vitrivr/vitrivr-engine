package org.vitrivr.engine.database.pgvector.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.LOGGER
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorWriter
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.SQLException
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
                stmt.setAny(1, item.id)
                stmt.setAny(2, item.retrievableId)
                for ((i, v) in item.values().withIndex()) {
                   stmt.setAny(3 + i, v)
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
                    stmt.setAny(1, item.id)
                    stmt.setAny(2, item.retrievableId)
                    for ((i, v) in item.values().withIndex()) {
                        stmt.setAny(3 + i, v.second)
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
                val values = item.values()
                stmt.setObject(1, item.retrievableId)
                for ((i, v) in values.withIndex()) {
                    stmt.setAny(2 + i, v)
                }
                stmt.setObject(1 + values.size, item.id)
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
    private fun PreparedStatement.setValue(index: Int, value: Value<*>) {
        when(value) {
            is Value.Boolean -> this.setBoolean(index, value.value)
            is Value.Byte -> this.setByte(index, value.value)
            is Value.DateTime -> this.setDate(index, Date(value.value.toInstant().toEpochMilli()))
            is Value.Double -> this.setDouble(index, value.value)
            is Value.Float -> this.setFloat(index, value.value)
            is Value.Int -> this.setInt(index, value.value)
            is Value.Long -> this.setLong(index, value.value)
            is Value.Short -> this.setShort(index, value.value)
            is Value.String -> this.setString(index, value.value)
        }
    }

    /**
     * Sets a value of [Any] type in a [PreparedStatement].
     */
    private fun PreparedStatement.setAny(index: Int, value: Any?) {
        when (value) {
            is String -> this.setString(index, value)
            is Int -> this.setInt(index, value)
            is Long -> this.setLong(index, value)
            is Float -> this.setFloat(index, value)
            is Double -> this.setDouble(index, value)
            is Boolean -> this.setBoolean(index, value)
            is ByteArray -> this.setBytes(index, value)
            is UUID -> this.setObject(index, value)
            is Value<*> -> this.setValue(index, value)
            else -> this.setObject(index, value)
        }
    }

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
        return this.connection.connection.prepareStatement(statement.toString())
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
        return this.connection.connection.prepareStatement(statement.toString())
    }
}