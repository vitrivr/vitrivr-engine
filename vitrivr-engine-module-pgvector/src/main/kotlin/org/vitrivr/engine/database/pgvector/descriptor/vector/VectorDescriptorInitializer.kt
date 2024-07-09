package org.vitrivr.engine.database.pgvector.descriptor.vector

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.database.pgvector.*
import org.vitrivr.engine.database.pgvector.descriptor.AbstractDescriptorInitializer
import java.sql.SQLException

/**
 * An [AbstractDescriptorInitializer] implementation for [VectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VectorDescriptorInitializer(field: Schema.Field<*, VectorDescriptor<*>>, connection: PgVectorConnection): AbstractDescriptorInitializer<VectorDescriptor<*>>(field, connection.jdbc) {
    /**
     * Initializes the [RetrievableInitializer].
     */
    override fun initialize() {
        try {
            val stmt = if (this.prototype.vector is Value.BooleanVector) {
                this.connection.prepareStatement(/* sql = postgres */ "CREATE TABLE IF NOT EXISTS $tableName ($DESCRIPTOR_ID_COLUMN_NAME uuid NOT NULL, $RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, $DESCRIPTOR_COLUMN_NAME bit(${this.prototype.dimensionality}) NOT NULL, PRIMARY KEY ($DESCRIPTOR_ID_COLUMN_NAME), FOREIGN KEY ($RETRIEVABLE_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME));")
            } else {
                this.connection.prepareStatement(/* sql = postgres */ "CREATE TABLE IF NOT EXISTS $tableName ($DESCRIPTOR_ID_COLUMN_NAME uuid NOT NULL, $RETRIEVABLE_ID_COLUMN_NAME uuid NOT NULL, $DESCRIPTOR_COLUMN_NAME vector(${this.prototype.dimensionality}) NOT NULL, PRIMARY KEY ($DESCRIPTOR_ID_COLUMN_NAME), FOREIGN KEY ($RETRIEVABLE_ID_COLUMN_NAME) REFERENCES $RETRIEVABLE_ENTITY_NAME($RETRIEVABLE_ID_COLUMN_NAME));")
            }
            stmt.use { it.execute() }
        } catch (e: SQLException) {
            LOGGER.error(e) { "Failed to initialize entity due to exception." }
        }
    }
}