package org.vitrivr.engine.core.database.retrievable

import org.vitrivr.engine.core.database.Writer
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * A [RetrievableWriter] is an extension of a [Writer] for [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 2.0.0
 */
interface RetrievableWriter : Writer<Retrievable> {
    /**
     * Persists a [Relationship].
     *
     * @param relationship [Relationship] to persist
     * @return True on success, false otherwise.
     */
    fun connect(relationship: Relationship): Boolean

    /**
     * Persists a list of [Relationship]s.
     *
     * @param relationships An [Iterable] of [Relationship]s to persist.
     * @return True on success, false otherwise.
     */
    fun connectAll(relationships: Iterable<Relationship>): Boolean

    /**
     * Deletes a [Relationship]
     *
     * @param relationship [Relationship] to delete.
     * @return True on success, false otherwise.
     */
    fun disconnect(relationship: Relationship): Boolean

    /**
     * Deletes a [Relationship]
     *
     * @param relationships An [Iterable] of [Relationship] to delete.
     * @return True on success, false otherwise.
     */
    fun disconnectAll(relationships: Iterable<Relationship>): Boolean
}