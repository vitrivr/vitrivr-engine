package org.vitrivr.engine.base.database.cottontail.writer

import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Field

/**
 * An [AbstractDescriptorWriter] for [FloatVectorDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorDescriptorWriter(field: Field<FloatVectorDescriptor>, connection: CottontailConnection): AbstractDescriptorWriter<FloatVectorDescriptor>(field, connection) {
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