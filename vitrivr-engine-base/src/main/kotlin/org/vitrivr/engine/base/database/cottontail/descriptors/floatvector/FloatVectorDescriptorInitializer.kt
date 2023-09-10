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
import org.vitrivr.engine.base.database.cottontail.initializer.AbstractDescriptorInitializer
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

private val logger: KLogger = KotlinLogging.logger {}


/**
 * A [AbstractDescriptorInitializer] implementation for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class FloatVectorDescriptorInitializer(field: Schema.Field<*, FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorInitializer<FloatVectorDescriptor>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        val example = this.field.analyser.prototype()
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName(DESCRIPTOR_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = true, autoIncrement = false)
            .column(Name.ColumnName(RETRIEVABLE_ID_COLUMN_NAME), Types.String, nullable = false, primaryKey = false, autoIncrement = false)
            .column(Name.ColumnName(FEATURE_COLUMN_NAME), Types.FloatVector(example.dimensionality), nullable = false, primaryKey = false, autoIncrement = false)

        try {
            this.connection.client.create(create)
        } catch (e: StatusException) {
            logger.error(e) { "Failed to initialize entity ${this.entityName} due to exception." }
        }
    }


}