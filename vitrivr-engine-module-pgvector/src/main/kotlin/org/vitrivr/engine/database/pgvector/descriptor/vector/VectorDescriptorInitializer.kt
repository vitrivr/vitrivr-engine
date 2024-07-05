package org.vitrivr.engine.database.pgvector.descriptor.vector

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorInitializer
import java.sql.SQLException

/**
 * An [AbstractDescriptorInitializer] implementation for [VectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VectorDescriptorInitializer(field: Schema.Field<*, VectorDescriptor<*>>, connection: PgVectorConnection): AbstractDescriptorInitializer<VectorDescriptor<*>>(field, connection.connection) {
    /**
     * Initializes the [RetrievableInitializer].
     */
    override fun initialize() {
        val type = this.field.analyser.prototype(this.field)
        try {
            /* Create 'retrievable' entity. */
            if (!this.connection.prepareStatement(/* sql = postgres */ "CREATE TABLE IF NOT EXISTS $tableName ($DESCRIPTOR_ID_COLUMN_NAME uuid NOT NULL, $RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, $DESCRIPTOR_COLUMN_NAME vector(${type.dimensionality}) NOT NULL, PRIMARY KEY ($DESCRIPTOR_ID_COLUMN_NAME), FOREIGN KEY ($RETRIEVABLE_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME));").use {
                    it.execute()
                }) {
                LOGGER.warn { "Failed to initialize entity '$tableName'. It probably exists." }
            }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to initialize entity due to exception." }
        }
    }
}