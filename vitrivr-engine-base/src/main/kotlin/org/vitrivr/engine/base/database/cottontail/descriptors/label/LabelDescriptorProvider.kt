package org.vitrivr.engine.base.database.cottontail.descriptors.label

import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.database.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [DescriptorProvider] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object LabelDescriptorProvider : DescriptorProvider<LabelDescriptor> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, LabelDescriptor>) = LabelDescriptorInitializer(field, connection as CottontailConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, LabelDescriptor>) = LabelDescriptorReader(field, connection as CottontailConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, LabelDescriptor>) = LabelDescriptorWriter(field, connection as CottontailConnection)
}