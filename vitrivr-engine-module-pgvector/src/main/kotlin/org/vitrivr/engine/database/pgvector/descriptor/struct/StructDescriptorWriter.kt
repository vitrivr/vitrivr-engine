package org.vitrivr.engine.database.pgvector.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.LOGGER
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorWriter
import java.sql.SQLException

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
                stmt.setObject(i, item.id)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to UPDATE descriptors in '$tableName' due to SQL error." }
            return false
        }
    }
}