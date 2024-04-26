package org.vitrivr.engine.core.model.relationship

import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * Used to model [Relationship]s between [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface Relationship: Persistable {
    /** The [RetrievableId] pointing to the subject [Retrievable]. */
    val subjectId: RetrievableId

    /** The named predicate. */
    val predicate: String

    /** The [RetrievableId] pointing to the object [Retrievable]. */
    val objectId: RetrievableId

    /**
     * Flag indicating, whether this [Relationship] is transient or persistent. The functionality of this flag is two-fold.
     *
     * - It indicates that a [Relationship] should (or shouldn't) be persisted (mostly during extraction).
     * - It indicates that a [Relationship] is backed by a persistent entry (mostly during indexing).
     */
    override val transient: Boolean

    /** A [Relationship] by reference to another [Retrievable]. */
    data class ByRef(val subject: Retrievable, override val predicate: String, val `object`: Retrievable, override val transient: Boolean):
        Relationship {
        override val subjectId: RetrievableId
            get() = this.subject.id

        override val objectId: RetrievableId
            get() = this.`object`.id
    }

    /** A [Relationship] by ID. */
    data class ById(override val subjectId: RetrievableId, override val predicate: String, override val objectId: RetrievableId, override val transient: Boolean):
        Relationship

    /** A [Relationship] where subject is provided by reference to another [Retrievable] and object by an ID. */
    data class BySubRefObjId(val subject: Retrievable, override val predicate: String, override val objectId: RetrievableId, override val transient: Boolean): Relationship {
        override val subjectId: RetrievableId
            get() = this.subject.id
    }

    /** A [Relationship] where subject is provided by ID and object by reference to another [Retrievable]. */
    data class BySubIdObjRef(override val subjectId: RetrievableId, override val predicate: String, val `object`: Retrievable, override val transient: Boolean): Relationship {
        override val objectId: RetrievableId
            get() = this.`object`.id
    }
}