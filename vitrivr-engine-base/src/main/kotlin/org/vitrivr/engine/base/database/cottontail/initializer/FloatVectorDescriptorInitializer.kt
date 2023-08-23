package org.vitrivr.engine.base.database.cottontail.initializer


import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.ddl.CreateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.types.Types
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [AbstractDescriptorInitializer] implementation for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class FloatVectorDescriptorInitializer(field: Schema.Field<FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorInitializer<FloatVectorDescriptor>(field, connection) {
    /**
     * Initializes the Cottontail DB entity backing this [AbstractDescriptorInitializer].
     */
    override fun initialize() {
        val example = this.field.analyser.newDescriptor(this.field) as? FloatVectorDescriptor ?: throw IllegalStateException("Expected float vector descriptor but received something else. This is a programmer's error!")
        val create = CreateEntity(this.entityName)
            .column(Name.ColumnName("id"), Types.String, false, true, false)
            .column(Name.ColumnName("retrievableId"), Types.String, false, false, false)
            .column(Name.ColumnName("vector"), Types.FloatVector(example.dimensionality), false, false, false)

        try {
            this.connection.client.create(create)
        } catch (e: StatusException) {
            /* TODO: Log. */
        }
    }
}