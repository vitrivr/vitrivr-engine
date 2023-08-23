package org.vitrivr.engine.base.database.cottontail.provider

import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.initializer.FloatVectorDescriptorInitializer
import org.vitrivr.engine.base.database.cottontail.reader.FloatVectorDescriptorReader
import org.vitrivr.engine.base.database.cottontail.writer.FloatVectorDescriptorWriter
import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [DescriptorProvider] for [FloatVectorDescriptor].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class FloatVectorDescriptorProvider: DescriptorProvider<FloatVectorDescriptor> {
    override fun newInitializer(connection: Connection, field: Schema.Field<FloatVectorDescriptor>) = FloatVectorDescriptorInitializer(field, connection as CottontailConnection)
    override fun newReader(connection: Connection, field: Schema.Field<FloatVectorDescriptor>) = FloatVectorDescriptorReader(field, connection as CottontailConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<FloatVectorDescriptor>) = FloatVectorDescriptorWriter(field, connection as CottontailConnection)
}