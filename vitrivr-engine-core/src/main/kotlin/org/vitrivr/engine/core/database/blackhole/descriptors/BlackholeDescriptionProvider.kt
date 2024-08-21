package org.vitrivr.engine.core.database.blackhole.descriptors

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [DescriptorProvider] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeDescriptionProvider<T: Descriptor<*>>: DescriptorProvider<T> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, T>)= BlackholeDescriptorInitializer(connection as BlackholeConnection, field)
    override fun newReader(connection: Connection, field: Schema.Field<*, T>): DescriptorReader<T> = BlackholeDescriptorReader(connection as BlackholeConnection, field)
    override fun newWriter(connection: Connection, field: Schema.Field<*, T>): DescriptorWriter<T> = BlackholeDescriptorWriter(connection as BlackholeConnection, field)
}