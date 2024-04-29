package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

class MockConnection(schemaName: String,
                     provider: ConnectionProvider = MockConnectionProvider(schemaName)
) : AbstractConnection(schemaName, provider) {
    override val provider: ConnectionProvider
        get() = TODO("Not yet implemented")
    override val schemaName: String
        get() = TODO("Not yet implemented")

    override fun initialize() {
        TODO("Not yet implemented")
    }

    override fun truncate() {
        TODO("Not yet implemented")
    }

    override fun getRetrievableInitializer(): RetrievableInitializer {
        TODO("Not yet implemented")
    }

    override fun getRetrievableWriter(): RetrievableWriter {
        TODO("Not yet implemented")
    }

    override fun getRetrievableReader(): RetrievableReader {
        TODO("Not yet implemented")
    }

    override fun <D : Descriptor> getDescriptorInitializer(field: Schema.Field<*, D>): DescriptorInitializer<D> {
        TODO("Not yet implemented")
    }

    override fun <D : Descriptor> getDescriptorWriter(field: Schema.Field<*, D>): DescriptorWriter<D> {
        TODO("Not yet implemented")
    }

    override fun <D : Descriptor> getDescriptorReader(field: Schema.Field<*, D>): DescriptorReader<D> {
        TODO("Not yet implemented")
    }

    override fun description(): String {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}
