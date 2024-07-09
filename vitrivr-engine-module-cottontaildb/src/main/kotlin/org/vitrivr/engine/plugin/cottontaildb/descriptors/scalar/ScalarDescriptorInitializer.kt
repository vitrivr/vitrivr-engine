package org.vitrivr.engine.plugin.cottontaildb.descriptors.scalar

import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorInitializer

/**
 * A [AbstractDescriptorInitializer] implementation for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
class ScalarDescriptorInitializer<T : ScalarDescriptor<*>>(field: Schema.Field<*, T>, connection: CottontailConnection) : AbstractDescriptorInitializer<T>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        /* Determine type based on prototype for Schema.Field. */
        val type = this.field.analyser.prototype(this.field).toType()

        /* Prepare query. */
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName.create(DESCRIPTOR_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName.create(RETRIEVABLE_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = false, autoIncrement = false)
            .column(Name.ColumnName.create(VALUE_ATTRIBUTE_NAME), type, nullable = false, primaryKey = false, autoIncrement = false)

        try {
            /* Try to create entity. */
            this.connection.client.create(create)
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }
}