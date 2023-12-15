package org.vitrivr.engine.base.database.cottontail.descriptors.string

import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
object StringDescriptorProvider : DescriptorProvider<StringDescriptor> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, StringDescriptor>) = StringDescriptorInitializer(field, connection as CottontailConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, StringDescriptor>) = StringDescriptorReader(field, connection as CottontailConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, StringDescriptor>) = TODO()
}