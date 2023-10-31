package org.vitrivr.engine.core.database.retrievable

import org.vitrivr.engine.core.database.Writer
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [RetrievableWriter] is an extension of a [Writer] for [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWriter : Writer<Retrievable> {
    /**
     * Connects two [Retrievable] (specified by their [RetrievableId]) through a subject predicate, object relationship.
     *
     * @param subject [RetrievableId] of the subject [Retrievable]
     * @param predicate The predicate describing the relationship.
     * @param object [RetrievableId] of the object [Retrievable]
     * @return True on success, false otherwise.
     */
    fun connect(subject: RetrievableId, predicate: String, `object`: RetrievableId): Boolean

    /**
     * Severs the specified connection between two [Retrievable]s.
     *
     * @param subject [RetrievableId] of the subject [Retrievable].
     * @param predicate The predicate describing the relationship.
     * @param object [RetrievableId] of the object [Retrievable].
     * @return True on success, false otherwise.
     */
    fun disconnect(subject: RetrievableId, predicate: String, `object`: RetrievableId): Boolean
}