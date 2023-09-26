package org.vitrivr.engine.base.database.cottontail.descriptors.scalar

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.descriptors.AbstractDescriptorInitializer
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.toType
import org.vitrivr.engine.core.model.database.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}

/**
 * A [AbstractDescriptorInitializer] implementation for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ScalarDescriptorInitializer<T : ScalarDescriptor<*>>(field: Schema.Field<*, T>, connection: CottontailConnection) : AbstractDescriptorInitializer<T>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        /* Determine type based on prototype for Schema.Field. */
        val type = this.field.analyser.prototype().toType()

        /* Prepare query. */
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName(CottontailConnection.DESCRIPTOR_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName(CottontailConnection.RETRIEVABLE_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = false, autoIncrement = false)
            .column(Name.ColumnName(DESCRIPTOR_COLUMN_NAME), type, nullable = false, primaryKey = false, autoIncrement = false)

        try {
            /* Try to create entity. */
            this.connection.client.create(create)
        } catch (e: StatusException) {
            logger.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }
}