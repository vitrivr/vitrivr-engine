package org.vitrivr.engine.database.jsonl

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

class JsonlProvider<D : Descriptor> : DescriptorProvider<D> {

    override fun newInitializer(connection: Connection, field: Schema.Field<*, D>): DescriptorInitializer<D> {
        TODO("Not yet implemented")
    }

    override fun newReader(connection: Connection, field: Schema.Field<*, D>): DescriptorReader<D> {
        TODO("Not yet implemented")
    }

    override fun newWriter(connection: Connection, field: Schema.Field<*, D>): DescriptorWriter<D> {
        TODO("Not yet implemented")
    }
}