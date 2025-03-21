package org.vitrivr.engine.core.database.blackhole.retrievable

import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved


/**
 * A [RetrievableReader] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class BlackholeRetrievableReader(override val connection: BlackholeConnection) : RetrievableReader {
    override fun get(id: RetrievableId): Retrieved? = null
    override fun exists(id: RetrievableId): Boolean = false
    override fun getAll(ids: Iterable<RetrievableId>) = emptySequence<Retrieved>()
    override fun getAll() = emptySequence<Retrieved>()
    override fun getConnections(subjectIds: Collection<RetrievableId>, predicates: Collection<String>, objectIds: Collection<RetrievableId>) = emptySequence<Relationship.ById>()
    override fun count(): Long = 0L
}