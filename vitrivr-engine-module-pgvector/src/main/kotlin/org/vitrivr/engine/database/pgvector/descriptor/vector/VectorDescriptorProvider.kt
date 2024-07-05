package org.vitrivr.engine.database.pgvector.descriptor.vector

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.PgVectorConnection

/**
 * A [DescriptorProvider] for [VectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object VectorDescriptorProvider: DescriptorProvider<VectorDescriptor<*>> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, VectorDescriptor<*>>) = VectorDescriptorInitializer(field, connection as PgVectorConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, VectorDescriptor<*>>) = VectorDescriptorReader(field, connection as PgVectorConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, VectorDescriptor<*>>) = VectorDescriptorWriter(field, connection as PgVectorConnection)
}