package org.vitrivr.engine.plugin.cottontaildb.descriptors.vector

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection
import org.vitrivr.engine.plugin.cottontaildb.descriptors.CottontailDescriptorInitializer
import org.vitrivr.engine.plugin.cottontaildb.descriptors.CottontailDescriptorWriter

/**
 * A [DescriptorProvider] for [VectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal object VectorDescriptorProvider : DescriptorProvider<VectorDescriptor<*, *>> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, VectorDescriptor<*, *>>) = CottontailDescriptorInitializer(field, connection as CottontailConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, VectorDescriptor<*, *>>) = VectorDescriptorReader(field, connection as CottontailConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, VectorDescriptor<*, *>>) = CottontailDescriptorWriter(field, connection as CottontailConnection)
}