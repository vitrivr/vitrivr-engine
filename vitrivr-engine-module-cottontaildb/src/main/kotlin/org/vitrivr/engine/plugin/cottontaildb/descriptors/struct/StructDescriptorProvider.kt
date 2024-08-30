package org.vitrivr.engine.plugin.cottontaildb.descriptors.struct

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection
import org.vitrivr.engine.plugin.cottontaildb.descriptors.CottontailDescriptorInitializer
import org.vitrivr.engine.plugin.cottontaildb.descriptors.CottontailDescriptorWriter

/**
 * A [DescriptorProvider] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
object StructDescriptorProvider : DescriptorProvider<StructDescriptor<*>> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, StructDescriptor<*>>) = CottontailDescriptorInitializer(field, connection as CottontailConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, StructDescriptor<*>>) = StructDescriptorReader(field, connection as CottontailConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, StructDescriptor<*>>) = CottontailDescriptorWriter(field, connection as CottontailConnection)
}