package org.vitrivr.engine.database.jsonl.retrievable

import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.database.jsonl.JsonlConnection

class JsonlRetrievableReader(override val connection: JsonlConnection) : RetrievableReader {

    override fun get(id: RetrievableId): Retrievable? {
        TODO("Not yet implemented")
    }

    override fun exists(id: RetrievableId): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAll(ids: Iterable<RetrievableId>): Sequence<Retrievable> {
        TODO("Not yet implemented")
    }

    override fun getConnections(
        subjectIds: Collection<RetrievableId>,
        predicates: Collection<String>,
        objectIds: Collection<RetrievableId>
    ): Sequence<Triple<RetrievableId, String, RetrievableId>> {
        TODO("Not yet implemented")
    }

    override fun getAll(): Sequence<Retrievable> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    fun close() {

    }
}