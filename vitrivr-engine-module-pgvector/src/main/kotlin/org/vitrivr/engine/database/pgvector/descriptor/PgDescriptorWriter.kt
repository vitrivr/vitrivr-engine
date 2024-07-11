package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.database.pgvector.*
import java.sql.*

/**
 * An abstract implementation of a [DescriptorWriter] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
open class PgDescriptorWriter<D : Descriptor>(final override val field: Schema.Field<*, D>, override val connection: PgVectorConnection): DescriptorWriter<D> {
    /** The name of the table backing this [PgDescriptorInitializer]. */
    protected val tableName: String = "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName}"

    /** The [Descriptor] prototype for this [PgDescriptorWriter]. */
    protected val prototype = this.field.analyser.prototype(this.field)

    /**
     * Adds (writes) a single [StructDescriptor] using this [StructDescriptorWriter].
     *
     * @param item The [StructDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: D): Boolean {
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
     * Adds (writes) a batch of [Descriptor] of type [D] using this [StructDescriptorWriter].
     *
     * @param items A [Iterable] of [Descriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<D>): Boolean {
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
     * Updates a specific [Descriptor] of type [D] using this [StructDescriptorWriter].
     *
     * @param item A [Descriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: D): Boolean {
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
                stmt.setObject(i, item.id)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to UPDATE descriptors in '$tableName' due to SQL error." }
            return false
        }
    }

    /**
     * Deletes (writes) a [Descriptor] of type [D] using this [PgDescriptorWriter].
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
     * Deletes (writes) [Descriptor]s of type [D] using this [PgDescriptorWriter].
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