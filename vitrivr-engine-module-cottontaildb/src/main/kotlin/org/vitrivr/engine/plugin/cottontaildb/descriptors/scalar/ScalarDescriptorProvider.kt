package org.vitrivr.engine.plugin.cottontaildb.descriptors.scalar

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.plugin.cottontaildb.CottontailConnection

/**
 * A [DescriptorProvider] for [ScalarDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal object ScalarDescriptorProvider : DescriptorProvider<ScalarDescriptor<*>> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, ScalarDescriptor<*>>) = ScalarDescriptorInitializer(field, connection as CottontailConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, ScalarDescriptor<*>>) = ScalarDescriptorReader(field, connection as CottontailConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, ScalarDescriptor<*>>) = ScalarDescriptorWriter(field, connection as CottontailConnection)
}