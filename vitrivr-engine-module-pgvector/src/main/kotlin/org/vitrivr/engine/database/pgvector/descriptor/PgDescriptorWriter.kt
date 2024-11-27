package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.*
import java.sql.*

/**
 * An abstract implementation of a [DescriptorWriter] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
open class PgDescriptorWriter<D : Descriptor<*>>(final override val field: Schema.Field<*, D>, override val connection: PgVectorConnection, protected val batchSize: Int = 1000) : DescriptorWriter<D> {

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
                for (attribute in item.layout()) {
                    val value = item.values()[attribute.name]
                    if (value != null) {
                        stmt.setValue(i++, value)
                    } else {
                        stmt.setNull(i++, attribute.type.toSql())
                    }
                }
                return stmt.executeUpdate() == 1
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to INSERT descriptor ${item.id} into \"${tableName.lowercase()}\" due to SQL error." }
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
            this.connection.jdbc.autoCommit = false
            var success = true
            var batched = 0
            this.prepareInsertStatement().use { stmt ->
                for (item in items) {
                    stmt.setObject(1, item.id)
                    stmt.setObject(2, item.retrievableId)
                    var i = 3
                    for (attribute in item.layout()) {
                        val value = item.values()[attribute.name]
                        if (value != null) {
                            stmt.setValue(i++, value)
                        } else {
                            stmt.setNull(i++, attribute.type.toSql())
                        }
                    }
                    stmt.addBatch()
                    batched += 1

                    /* Execute batch if necessary. */
                    if (batched % this.batchSize == 0) {
                        val results = stmt.executeBatch()
                        batched = 0
                        stmt.clearBatch()
                        if (results.any { it != 1 }) {
                            success = false
                            break
                        }
                    }
                }

                /* Execute remaining batch and commit. */
                if (batched > 0) {
                    success = stmt.executeBatch().all { it == 1 }
                }
                if (success) {
                    this.connection.jdbc.commit()
                } else {
                    this.connection.jdbc.rollback()
                }
                return success
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to INSERT descriptors into \"${tableName.lowercase()}\" due to SQL error." }
            this.connection.jdbc.rollback()
            return false
        } finally {
            this.connection.jdbc.autoCommit = true
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
                for (attribute in item.layout()) {
                    val value = item.values()[attribute.name]
                    if (value != null) {
                        stmt.setValue(i++, value)
                    } else {
                        stmt.setNull(i++, attribute.type.toSql())
                    }
                }
                stmt.setObject(i, item.id)
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to UPDATE descriptors in \"${tableName.lowercase()}\" due to SQL error." }
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
            this.connection.jdbc.prepareStatement("DELETE FROM \"${tableName.lowercase()}\" WHERE $DESCRIPTOR_ID_COLUMN_NAME = ?;").use { stmt ->
                stmt.setObject(1, item.id)
                return stmt.executeUpdate() == 1
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
            this.connection.jdbc.prepareStatement("DELETE FROM \"${tableName.lowercase()}\" WHERE $DESCRIPTOR_ID_COLUMN_NAME = ANY (?);").use { stmt ->
                val values = items.map { it.id }.toTypedArray()
                stmt.setArray(1, this.connection.jdbc.createArrayOf("uuid", values))
                return stmt.executeUpdate() > 0
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to delete descriptors due to SQL error." }
            return false
        }
    }


    /**
     * Prepares an INSERT statement for this [StructDescriptorWriter].
     *
     * @return [PreparedStatement]
     */
    protected fun prepareUpdateStatement(): PreparedStatement {
        val statement = StringBuilder("UPDATE \"${tableName.lowercase()}\" SET $RETRIEVABLE_ID_COLUMN_NAME = ?")
        for (field in this.prototype.layout()) {
            statement.append(", \"${field.name.lowercase()}\" = ?")
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
        val statement = StringBuilder("INSERT INTO \"${tableName.lowercase()}\" ($DESCRIPTOR_ID_COLUMN_NAME, $RETRIEVABLE_ID_COLUMN_NAME")
        for (field in this.prototype.layout()) {
            statement.append(", \"${field.name.lowercase()}\"")
        }
        statement.append(") VALUES (?, ?")
        for (field in this.field.analyser.prototype(this.field).layout()) {
            statement.append(", ?")
        }
        statement.append(");")
        return this.connection.jdbc.prepareStatement(statement.toString())
    }
}