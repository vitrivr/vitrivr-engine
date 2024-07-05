package org.vitrivr.engine.database.pgvector.descriptor

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ENTITY_PREFIX
import org.vitrivr.engine.database.pgvector.LOGGER
import java.sql.Connection
import java.sql.SQLException

/**
 * An abstract implementation of a [DescriptorInitializer] for PostgreSQL with pgVector.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorInitializer<D : Descriptor>(final override val field: Schema.Field<*, D>, protected val connection: Connection): DescriptorInitializer<D> {

    /** The name of the table backing this [AbstractDescriptorInitializer]. */
    protected val tableName: String = "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName}"

    /**
     * Checks if the schema for this [AbstractDescriptorInitializer] has been properly initialized.
     *
     * @return True if entity has been initialized, false otherwise.
     */
    override fun isInitialized(): Boolean {
        try {
            this.connection.prepareStatement(/* sql = postgres */ "SELECT count(*) FROM $tableName").use {
                it.execute()
            }
        } catch (e: SQLException) {
            return false
        }
        return true
    }

    /**
     * Truncates the table backing this [AbstractDescriptorInitializer].
     */
    override fun truncate() {
        try {
            this.connection.prepareStatement(/* sql = postgres */ "TRUNCATE $tableName").use {
                it.execute()
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to truncate entities due to exception." }
        }
    }
}