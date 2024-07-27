package org.vitrivr.engine.database.jsonl

import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved

class JsonlReader<D : Descriptor>(override val field: Schema.Field<*, D>, override val connection: JsonlConnection) :
    DescriptorReader<D> {
    override fun exists(descriptorId: DescriptorId): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(descriptorId: DescriptorId): D? {
        TODO("Not yet implemented")
    }

    override fun getAll(descriptorIds: Iterable<DescriptorId>): Sequence<D> {
        TODO("Not yet implemented")
    }

    override fun getAll(): Sequence<D> {
        TODO("Not yet implemented")
    }

    override fun getForRetrievable(retrievableId: RetrievableId): Sequence<D> {
        TODO("Not yet implemented")
    }

    override fun getAllForRetrievable(retrievableIds: Iterable<RetrievableId>): Sequence<D> {
        TODO("Not yet implemented")
    }

    override fun query(query: Query): Sequence<D> {
        TODO("Not yet implemented")
    }

    override fun queryAndJoin(query: Query): Sequence<Retrieved> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }
}