package org.vitrivr.engine.core.model.database.retrievable

/**
 * A [Retrievable] with relationships to other [Retrievable]s
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithRelationship : Retrievable {
    /** [Set] of [Retrievable]s, this [Retrievable] is a part of. May be empty! */
    val partOf: Set<Retrievable>

    /** [Set] of [Retrievable]s, that make-up this [Retrievable]. May be empty! */
    val parts: Set<Retrievable>

    /**
     * A [Mutable] version of the [RetrievableWithRelationship].
     */
    interface Mutable : RetrievableWithRelationship {
        /* TODO */
    }
}