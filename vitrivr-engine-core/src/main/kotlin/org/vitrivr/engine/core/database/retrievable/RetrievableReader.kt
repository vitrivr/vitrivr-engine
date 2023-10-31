package org.vitrivr.engine.core.database.retrievable

import org.vitrivr.engine.core.database.Reader
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [RetrievableWriter] is an extension of a [Retrievable] for [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableReader : Reader<Retrievable> {

    fun getConnections(
        subjectIds: Collection<RetrievableId>,
        predicates: Collection<String>,
        objectIds: Collection<RetrievableId>
    ): Sequence<Triple<RetrievableId, String, RetrievableId>>

}