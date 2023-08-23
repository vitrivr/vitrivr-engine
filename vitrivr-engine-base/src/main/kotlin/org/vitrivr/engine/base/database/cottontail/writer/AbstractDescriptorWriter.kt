package org.vitrivr.engine.base.database.cottontail.writer

import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * An abstract implementation of a [DescriptorWriter] for Cottontail DB.

 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorWriter<T: Descriptor>(final override val field: Schema.Field<T>, protected val connection: CottontailConnection): DescriptorWriter<T> {
    /** The [Name.EntityName] used by this [FloatVectorDescriptor]. */
    protected val entityName: Name.EntityName = Name.EntityName(this.field.schema.name, "${CottontailConnection.DESCRIPTOR_ENTITY_PREFIX}_${this.field.fieldName.lowercase()}")
}