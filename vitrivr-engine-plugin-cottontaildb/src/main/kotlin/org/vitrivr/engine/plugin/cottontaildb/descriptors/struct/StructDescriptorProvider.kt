package org.vitrivr.engine.plugin.cottontaildb.descriptors.struct

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection

/**
 * A [DescriptorProvider] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object StructDescriptorProvider : DescriptorProvider<StructDescriptor> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, StructDescriptor>) = StructDescriptorInitializer(field, connection as CottontailConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, StructDescriptor>) = StructDescriptorReader(field, connection as CottontailConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, StructDescriptor>) = StructDescriptorWriter(field, connection as CottontailConnection)
}