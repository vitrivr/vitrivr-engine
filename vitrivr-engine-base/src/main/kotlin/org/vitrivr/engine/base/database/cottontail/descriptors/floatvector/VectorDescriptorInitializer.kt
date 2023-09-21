package org.vitrivr.engine.base.database.cottontail.descriptors.floatvector

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.DESCRIPTOR_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.descriptors.toType
import org.vitrivr.engine.base.database.cottontail.initializer.AbstractDescriptorInitializer
import org.vitrivr.engine.core.model.database.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}


/**
 * A [AbstractDescriptorInitializer] implementation for [VectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class VectorDescriptorInitializer(field: Schema.Field<*, VectorDescriptor<*>>, connection: CottontailConnection) : AbstractDescriptorInitializer<VectorDescriptor<*>>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        val type = this.field.analyser.prototype().toType()
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName(DESCRIPTOR_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName(RETRIEVABLE_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = false, autoIncrement = false)
            .column(Name.ColumnName(DESCRIPTOR_COLUMN_NAME), type, nullable = false, primaryKey = false, autoIncrement = false)

        try {
            this.connection.client.create(create)
        } catch (e: StatusException) {
            logger.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }
}