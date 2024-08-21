package org.vitrivr.engine.core.database.blackhole.descriptors

import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved

/**
 * A [DescriptorReader] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeDescriptorReader<T: Descriptor<*>>(override val connection: BlackholeConnection, override val field: Schema.Field<*, T>) : DescriptorReader<T> {
    override fun exists(descriptorId: DescriptorId): Boolean = false
    override fun get(descriptorId: DescriptorId): T? = null
    override fun getAll(descriptorIds: Iterable<DescriptorId>) = emptySequence<T>()
    override fun getAll() = emptySequence<T>()
    override fun getForRetrievable(retrievableId: RetrievableId) = emptySequence<T>()
    override fun getAllForRetrievable(retrievableIds: Iterable<RetrievableId>) = emptySequence<T>()
    override fun query(query: Query) = emptySequence<T>()
    override fun queryAndJoin(query: Query) = emptySequence<Retrieved>()
    override fun count() = 0L
}