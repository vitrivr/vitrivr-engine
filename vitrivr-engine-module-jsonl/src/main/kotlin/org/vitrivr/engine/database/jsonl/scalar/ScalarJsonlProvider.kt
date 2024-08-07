package org.vitrivr.engine.database.jsonl.scalar

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.database.jsonl.AbstractJsonlProvider
import org.vitrivr.engine.database.jsonl.JsonlConnection

object ScalarJsonlProvider: AbstractJsonlProvider<ScalarDescriptor<*>>() {
    override fun newReader(connection: Connection, field: Schema.Field<*, ScalarDescriptor<*>>): DescriptorReader<ScalarDescriptor<*>> = ScalarJsonlReader(field, connection as JsonlConnection)
}