package org.vitrivr.engine.index.database.string.writer

import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.index.database.string.StringConnection

class StringDescriptorWriter<D : Descriptor>(connection: StringConnection, override val field: Schema.Field<*,D>) : DescriptorWriter<D>, StringWriter<D>(connection.provider.targetStream, connection.stringify) {
}