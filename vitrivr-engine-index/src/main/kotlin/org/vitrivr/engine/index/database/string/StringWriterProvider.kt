package org.vitrivr.engine.index.database.string

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.database.descriptor.DescriptorProvider
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.index.database.string.writer.StringDescriptorWriter
import org.vitrivr.engine.index.database.string.writer.StringWriter
import org.vitrivr.engine.index.database.util.initializer.NoDescriptorInitializer
import org.vitrivr.engine.index.database.util.initializer.NoInitializer

class StringWriterProvider<D: Descriptor> : DescriptorProvider<D> {

    override fun newInitializer(connection: Connection, field: Schema.Field<*,D>): NoDescriptorInitializer<D> = NoDescriptorInitializer(field)

    override fun newReader(connection: Connection, field: Schema.Field<*,D>): DescriptorReader<D> {
        throw UnsupportedOperationException("StringWriter is append only")
    }

    override fun newWriter(connection: Connection, field: Schema.Field<*,D>): DescriptorWriter<D> = StringDescriptorWriter(connection as StringConnection, field)

}