package org.vitrivr.engine.plugin.cottontaildb.descriptors.struct

import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.*
import org.vitrivr.engine.plugin.cottontaildb.LOGGER
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorInitializer
import org.vitrivr.engine.plugin.cottontaildb.toCottontailType

/**
 * A [AbstractDescriptorInitializer] implementation for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class StructDescriptorInitializer(field: Schema.Field<*, StructDescriptor>, connection: CottontailConnection) : AbstractDescriptorInitializer<StructDescriptor>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        /* Prepare query. */
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName.create(DESCRIPTOR_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName.create(RETRIEVABLE_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = false, autoIncrement = false)

        for (field in this.field.analyser.prototype(this.field).schema()) {
            create.column(Name.ColumnName.create(field.name), field.type.toCottontailType(), nullable = field.nullable, primaryKey = false, autoIncrement = false)
        }

        try {
            /* Try to create entity. */
            this.connection.client.create(create)
        } catch (e: StatusRuntimeException) {
            LOGGER.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }
}
