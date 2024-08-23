package org.vitrivr.engine.core.database.blackhole.retrievable

import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId


/**
 * A [RetrievableReader] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeRetrievableReader(override val connection: BlackholeConnection) : RetrievableReader {
    override fun get(id: RetrievableId): Retrievable? = null
    override fun exists(id: RetrievableId): Boolean =  false
    override fun getAll(ids: Iterable<RetrievableId>) = emptySequence<Retrievable>()
    override fun getAll() = emptySequence<Retrievable>()
    override fun getConnections(subjectIds: Collection<RetrievableId>, predicates: Collection<String>, objectIds: Collection<RetrievableId>) = emptySequence<Triple<RetrievableId, String, RetrievableId>>()
    override fun count(): Long = 0L
}