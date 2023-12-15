package org.vitrivr.engine.plugin.cottontaildb.descriptors.string

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.client.language.ddl.CreateIndex
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.cottontail.grpc.CottontailGrpc
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection
import org.vitrivr.engine.plugin.cottontaildb.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.plugin.cottontaildb.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.plugin.cottontaildb.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.plugin.cottontaildb.descriptors.AbstractDescriptorInitializer

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [AbstractDescriptorInitializer] implementation for [StringDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class StringDescriptorInitializer(field: Schema.Field<*, StringDescriptor>, connection: CottontailConnection) : AbstractDescriptorInitializer<StringDescriptor>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        /* Prepare to create entity. */
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName(DESCRIPTOR_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName(RETRIEVABLE_ID_COLUMN_NAME), Types.Uuid, nullable = false, primaryKey = false, autoIncrement = false)
            .column(Name.ColumnName(DESCRIPTOR_COLUMN_NAME), Types.String, nullable = false, primaryKey = false, autoIncrement = false)

        try {
            /* Try to create entity. */
            this.connection.client.create(create)

            /* Create entity if necessary. */
            if (this.field.parameters.containsKey("index")) {
                val createIndex = CreateIndex(this.entityName, CottontailGrpc.IndexType.LUCENE)
                    .column(this.entityName.column(DESCRIPTOR_COLUMN_NAME))
                this.connection.client.create(createIndex)
            }
        } catch (e: StatusRuntimeException) {
            logger.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }
}