package org.vitrivr.engine.plugin.cottontaildb.descriptors

import io.grpc.StatusRuntimeException
import org.vitrivr.cottontail.client.language.ddl.ListEntities
import org.vitrivr.cottontail.client.language.ddl.TruncateEntity
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection
import org.vitrivr.engine.plugin.cottontaildb.DESCRIPTOR_ENTITY_PREFIX

/**
 * An abstract implementation of a [DescriptorInitializer] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorInitializer<D : Descriptor>(final override val field: Schema.Field<*, D>, protected val connection: CottontailConnection) : DescriptorInitializer<D> {
    /** The [Name.EntityName] used by this [Descriptor]. */
    protected val entityName: Name.EntityName = Name.EntityName.create(this.field.schema.name, "${DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")

    /**
     * Checks if the schema for this [AbstractDescriptorInitializer] has been properly initialized.
     *
     * @return True if entity has been initialized, false otherwise.
     */
    override fun isInitialized(): Boolean = try {
        this.connection.client.list(ListEntities(this.entityName.schema)).asSequence().any {
            Name.EntityName.parse(it.asString(0)!!) == this.entityName
        }
    } catch (e: StatusRuntimeException) {
        false
    }

    /**
     * Truncates the entity backing this [AbstractDescriptorInitializer].
     */
    override fun truncate() {
        val truncate = TruncateEntity(this.entityName)
        try {
            this.connection.client.truncate(truncate).use {
                it.hasNext()
            }
        } catch (e: StatusRuntimeException) {
            /* TODO: Log. */
        }
    }
}