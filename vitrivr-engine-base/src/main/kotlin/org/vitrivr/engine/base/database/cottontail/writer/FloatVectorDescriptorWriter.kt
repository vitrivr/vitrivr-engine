package org.vitrivr.engine.base.database.cottontail.writer

import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.reader.AbstractDescriptorReader
import org.vitrivr.engine.core.database.Writer
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.operators.Describer

/**
 * An [AbstractDescriptorWriter] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorDescriptorWriter(describer: Describer<FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorWriter<FloatVectorDescriptor>(describer, connection) {
    override fun add(item: Descriptor): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(items: Iterable<Descriptor>): Boolean {
        TODO("Not yet implemented")
    }

    override fun update(item: Descriptor): Boolean {
        TODO("Not yet implemented")
    }

    override fun delete(item: Descriptor): Boolean {
        TODO("Not yet implemented")
    }
}