package org.vitrivr.engine.database.pgvector.descriptor.providers

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.descriptor.PgDescriptorInitializer
import org.vitrivr.engine.database.pgvector.descriptor.PgDescriptorReader
import org.vitrivr.engine.database.pgvector.descriptor.PgDescriptorWriter

/**
 * A [DescriptorProvider] for [StructDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object ScalarDescriptorProvider : DescriptorProvider<ScalarDescriptor<*, *>> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, ScalarDescriptor<*, *>>) = PgDescriptorInitializer(field, connection as PgVectorConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, ScalarDescriptor<*, *>>) = PgDescriptorReader(field, connection as PgVectorConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, ScalarDescriptor<*, *>>) = PgDescriptorWriter(field, connection as PgVectorConnection)
}