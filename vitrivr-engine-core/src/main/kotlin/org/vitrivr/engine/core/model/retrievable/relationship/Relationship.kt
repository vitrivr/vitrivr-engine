package org.vitrivr.engine.core.model.retrievable.relationship

import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId


/**
 * Used to model [Relationship]s between [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface Relationship {
    /** The [RetrievableId] pointing to the subject [Retrievable]. */
    val subjectId: RetrievableId

    /** The named predicate. */
    val predicate: String

    /** The [RetrievableId] pointing to the object [Retrievable]. */
    val objectId: RetrievableId

    /** A [Relationship] by reference to another [Retrievable]. */
    data class ByRef(val subject: Retrievable, override val predicate: String, val `object`: Retrievable): Relationship {
        override val subjectId: RetrievableId
            get() = this.subject.id

        override val objectId: RetrievableId
            get() = this.`object`.id
    }

    /** A [Relationship] by ID. */
    data class ById(override val subjectId: RetrievableId, override val predicate: String, override val objectId: RetrievableId): Relationship
}