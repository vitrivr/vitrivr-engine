package org.vitrivr.engine.core.model.database.retrievable

/**
 * A [Retrievable] that has a distance associated with it.
 *
 * Often returned during retrieval after a proximity based query.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithDistance : Retrievable {
    /**
     * The distance associated with this [RetrievableWithDistance].
     *
     * Distances are strictly positive but potentially unbounded.
     */
    val distance: Float
}