package org.vitrivr.engine.database.pgvector.descriptor.struct

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.pgvector.PgVectorConnection
import org.vitrivr.engine.database.pgvector.descriptor.PgDescriptorInitializer
import org.vitrivr.engine.database.pgvector.descriptor.PgDescriptorWriter

/**
 * A [DescriptorProvider] for [LabelDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object StructDescriptorProvider : DescriptorProvider<StructDescriptor> {
    override fun newInitializer(connection: Connection, field: Schema.Field<*, StructDescriptor>) = PgDescriptorInitializer(field, connection as PgVectorConnection)
    override fun newReader(connection: Connection, field: Schema.Field<*, StructDescriptor>) = StructDescriptorReader(field, connection as PgVectorConnection)
    override fun newWriter(connection: Connection, field: Schema.Field<*, StructDescriptor>) = PgDescriptorWriter(field, connection as PgVectorConnection)
}