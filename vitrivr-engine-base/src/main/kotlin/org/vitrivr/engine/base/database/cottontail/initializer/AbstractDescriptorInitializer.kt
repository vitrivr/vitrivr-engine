package org.vitrivr.engine.base.database.cottontail.initializer

import io.grpc.StatusException
import org.vitrivr.cottontail.client.language.ddl.TruncateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Field

/**
 * An abstract implementation of a [DescriptorInitializer] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorInitializer<T: Descriptor>(final override val field: Field<T>, protected val connection: CottontailConnection): DescriptorInitializer<T> {
    /** The [Name.EntityName] used by this [FloatVectorDescriptor]. */
    protected val entityName: Name.EntityName = this.connection.schemaName.entity("${CottontailConnection.DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")

    /**
     * Truncates the entity backing this [AbstractDescriptorInitializer].
     */
    override fun truncate() {
        val truncate = TruncateEntity(this.entityName)
        try {
            this.connection.client.truncate(truncate)
        } catch (e: StatusException) {
            /* TODO: Log. */
        }
    }
}