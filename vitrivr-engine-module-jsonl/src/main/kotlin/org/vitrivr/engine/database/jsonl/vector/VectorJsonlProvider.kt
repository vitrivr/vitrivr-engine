package org.vitrivr.engine.database.jsonl.vector

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.jsonl.AbstractJsonlProvider
import org.vitrivr.engine.database.jsonl.JsonlConnection

object VectorJsonlProvider: AbstractJsonlProvider<VectorDescriptor<*>>() {
    override fun newReader(connection: Connection, field: Schema.Field<*, VectorDescriptor<*>>): DescriptorReader<VectorDescriptor<*>> = VectorJsonlReader(field, connection as JsonlConnection)
}