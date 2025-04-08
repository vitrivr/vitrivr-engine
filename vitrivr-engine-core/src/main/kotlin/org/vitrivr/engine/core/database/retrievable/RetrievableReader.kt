package org.vitrivr.engine.core.database.retrievable

import org.vitrivr.engine.core.database.Reader
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import java.util.*

/**
 * A [RetrievableWriter] is an extension of a [Retrievable] for [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
interface RetrievableReader : Reader<Retrieved> {
    /**
     * Returns the [Retrievable] that corresponds to the provided [RetrievableId].
     *
     * @param id The [RetrievableId] to return.
     * @return [Retrievable] or null
     */
    operator fun get(id: RetrievableId): Retrieved?

    /**
     * Checks if a [Retrievable] [RetrievableId] exists.
     *
     * @param id The [RetrievableId] to check for.
     * @return True if [Retrieved] exists, false otherwise.
     */
    fun exists(id: RetrievableId): Boolean

    /**
     * Returns a [Sequence] of all [Retrievable] accessible by this [Reader] whose [RetrievableId] is contained in the provided [Iterable].
     *
     * @param ids A [Iterable] of [RetrievableId]s to return.
     * @return [Sequence] of [Retrieved]
     */
    fun getAll(ids: Iterable<RetrievableId>): Sequence<Retrieved>

    /**
     * Returns a [Retrievable] of type [T] that corresponds to the provided [UUID] in a specified columnName.
     *
     * @param subjectIds The [UUID]s of the subjects to consider. If empty, all subjects are considered.
     * @param predicates The predicates to consider.
     * @param objectIds The [UUID]s of the objects to consider.  If empty, all subjects are considered.
     */
    fun getConnections(subjectIds: Collection<RetrievableId>, predicates: Collection<String>, objectIds: Collection<RetrievableId>): Sequence<Relationship.ById>
}