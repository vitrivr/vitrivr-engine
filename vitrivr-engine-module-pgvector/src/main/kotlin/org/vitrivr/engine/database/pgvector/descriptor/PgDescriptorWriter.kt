package org.vitrivr.engine.database.pgvector.descriptor

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.tables.AbstractDescriptorTable

/**
 * An abstract implementation of a [DescriptorWriter] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
open class PgDescriptorWriter<D : Descriptor<*>>(final override val field: Schema.Field<*, D>, override val connection: PgVectorConnection) : DescriptorWriter<D> {

    /** The [AbstractDescriptorTable] backing this [PgDescriptorInitializer]. */
    protected val table: AbstractDescriptorTable<D> = this.field.toTable()

    /** The name of the table backing this [PgDescriptorWriter]. */
    protected val tableName: String
        get() = this.table.nameInDatabaseCase()

    /**
     * Adds (writes) a single [Descriptor] using this [PgDescriptorWriter].
     *
     * @param item The [Descriptor] to write.
     * @return True on success, false otherwise.
     */
    override fun add(item: D): Boolean = transaction(this.connection.database) {
        try {
            this@PgDescriptorWriter.table.insert(item)
            true
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to INSERT descriptor ${item.id} into '${tableName}' due to SQL error." }
            false
        }
    }

    /**
     * Adds (writes) a batch of [Descriptor] of type [D] using this [PgDescriptorWriter].
     *
     * @param items A [Iterable] of [Descriptor]s to write.
     * @return True on success, false otherwise.
     */
    override fun addAll(items: Iterable<D>): Boolean = transaction(this.connection.database) {
        try {
            this@PgDescriptorWriter.table.batchInsert(items)
            true
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to INSERT descriptors into '${tableName}' due to SQL error." }
            false
        }
    }

    /**
     * Updates a specific [Descriptor] of type [D] using this [DescriptorWriter].
     *
     * @param item A [Descriptor]s to update.
     * @return True on success, false otherwise.
     */
    override fun update(item: D): Boolean = transaction(this.connection.database) {
        try {
            this@PgDescriptorWriter.table.update(item) > 0
        } catch (e: Throwable) {
            LOGGER.error(e) { "Failed to UPDATE descriptors in \"${tableName.lowercase()}\" due to SQL error." }
            false
        }
    }

    /**
     * Deletes (writes) a [Descriptor] of type [D] using this [PgDescriptorWriter].
     *
     * @param item A [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: D): Boolean = transaction(this.connection.database) {
        try {
            this@PgDescriptorWriter.table.deleteWhere { this@PgDescriptorWriter.table.id eq item.id } > 0
        } catch (e: Throwable) {
            LOGGER.error(e) {  "Failed to delete descriptor ${item.id} due to SQL error." }
            false
        }
    }

    /**
     * Deletes (writes) [Descriptor]s of type [D] using this [PgDescriptorWriter].
     *
     * @param items A [Iterable] of [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun deleteAll(items: Iterable<D>): Boolean = transaction(this.connection.database) {
        try {
            this@PgDescriptorWriter.table.deleteWhere { this@PgDescriptorWriter.table.id inList items.map { it.id } } > 0
        } catch (e: Throwable) {
            LOGGER.error(e) {  "Failed to delete descriptors due to SQL error." }
            false
        }
    }
}