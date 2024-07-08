package org.vitrivr.engine.database.pgvector.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorWriter
import org.vitrivr.engine.database.pgvector.descriptor.model.PgVector
import java.sql.SQLException

/**
 * An [AbstractDescriptorWriter] for [VectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VectorDescriptorWriter(field: Schema.Field<*, VectorDescriptor<*>>, connection: PgVectorConnection) : AbstractDescriptorWriter<VectorDescriptor<*>>(field, connection) {
    /**
     * Adds (writes) a single [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param item The [VectorDescriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: VectorDescriptor<*>): Boolean {
        try {
            this.connection.jdbc.prepareStatement("INSERT INTO $tableName ($DESCRIPTOR_ID_COLUMN_NAME, $RETRIEVABLE_ID_COLUMN_NAME, $DESCRIPTOR_COLUMN_NAME) VALUES (?, ?, ?);").use { stmt ->
                stmt.setObject(1, item.id)
                stmt.setObject(2, item.retrievableId)
                stmt.setObject(3, PgVector(item.vector))
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to persist descriptor ${item.id} due to SQL error." }
            return false
        }
    }

    /**
     * Adds (writes) a batch of [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param items A [Iterable] of [VectorDescriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<VectorDescriptor<*>>): Boolean {
        try {
            this.connection.jdbc.prepareStatement("INSERT INTO $tableName ($DESCRIPTOR_ID_COLUMN_NAME, $RETRIEVABLE_ID_COLUMN_NAME, $DESCRIPTOR_COLUMN_NAME) VALUES (?, ?, ?);").use { stmt ->
                for (item in items) {
                    stmt.setObject(1, item.id)
                    stmt.setObject(2, item.retrievableId)
                    stmt.setObject(3, PgVector(item.vector))
                    stmt.addBatch()
                }
                return stmt.executeBatch().all { it == 1 }
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to persist descriptors due to SQL error." }
            return false
        }
    }

    /**
     * Updates a specific [VectorDescriptor] using this [VectorDescriptorWriter].
     *
     * @param item A [VectorDescriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: VectorDescriptor<*>): Boolean {
        try {
            this.connection.jdbc.prepareStatement("UPDATE $tableName SET $RETRIEVABLE_ID_COLUMN_NAME = ?,  $DESCRIPTOR_COLUMN_NAME = ? WHERE $RETRIEVABLE_ID_COLUMN_NAME = ?;").use { stmt ->
                stmt.setObject(1, item.retrievableId)
                stmt.setObject(2, PgVector(item.vector))
                stmt.setObject(3, item.id)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to persist descriptors due to SQL error." }
            return false
        }
    }
}