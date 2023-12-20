package org.vitrivr.engine.base.database.string.writer

import org.vitrivr.engine.base.database.string.StringConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

class StringDescriptorWriter<D : Descriptor>(connection: StringConnection, override val field: Schema.Field<*,D>) : DescriptorWriter<D>, StringWriter<D>(connection.provider.targetStream, connection.stringify) {
}