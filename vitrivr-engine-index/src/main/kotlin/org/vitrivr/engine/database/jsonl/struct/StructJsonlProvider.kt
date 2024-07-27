package org.vitrivr.engine.database.jsonl.struct

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.jsonl.AbstractJsonlProvider
import org.vitrivr.engine.database.jsonl.JsonlConnection

object StructJsonlProvider: AbstractJsonlProvider<StructDescriptor>() {
    override fun newReader(connection: Connection, field: Schema.Field<*, StructDescriptor>): DescriptorReader<StructDescriptor> = StructJsonlReader(field, connection as JsonlConnection)
}