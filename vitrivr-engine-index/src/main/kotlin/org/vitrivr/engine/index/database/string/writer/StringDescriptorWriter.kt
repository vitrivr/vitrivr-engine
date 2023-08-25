package org.vitrivr.engine.index.database.string.writer

import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.index.database.string.StringConnection

class StringDescriptorWriter<T : Descriptor>(connection: StringConnection, override val field: Schema.Field<T>) : DescriptorWriter<T>, StringWriter<T>(connection.provider.targetStream, connection.stringify) {
}