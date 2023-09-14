package org.vitrivr.engine.base.database.string.writer

import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.base.database.string.StringConnection

class StringDescriptorWriter<D : Descriptor>(connection: StringConnection, override val field: Schema.Field<*,D>) : DescriptorWriter<D>, StringWriter<D>(connection.provider.targetStream, connection.stringify) {
}