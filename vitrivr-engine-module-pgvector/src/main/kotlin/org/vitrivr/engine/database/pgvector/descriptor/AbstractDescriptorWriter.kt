package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ENTITY_PREFIX
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.LOGGER
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import java.sql.SQLException

/**
 * An abstract implementation of a [DescriptorWriter] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorWriter<D : Descriptor>(final override val field: Schema.Field<*, D>, protected val connection: PgVectorConnection): DescriptorWriter<D> {
    /** The name of the table backing this [AbstractDescriptorInitializer]. */
    protected val tableName: String = "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName}"

    /**
     * Deletes (writes) a [Descriptor] of type [D] using this [AbstractDescriptorWriter].
     *
     * @param item A [Descriptor]s to delete.
     * @return True on success, false otherwise.
     */
    override fun delete(item: D): Boolean {
        try {
            this.connection.connection.prepareStatement("DELETE FROM $tableName WHERE $DESCRIPTOR_ID_COLUMN_NAME = ?;").use { stmt ->
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
            this.connection.connection.prepareStatement("DELETE FROM $tableName WHERE $DESCRIPTOR_ID_COLUMN_NAME = ANY (?);").use { stmt ->
                val values = items.map { it.id }.toTypedArray()
                stmt.setArray(1, this.connection.connection.createArrayOf("uuid", values))
                return stmt.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to delete descriptors due to SQL error." }
            return false
        }
    }
}