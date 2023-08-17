package org.vitrivr.engine.core.model.database.retrievable

import java.util.*

/**
 * A [Retrievable] that has been scored as part of the query process. Used in the query pipeline.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface ScoredRetrievable : Retrievable {

    /** The score of this [ScoredRetrievable]. */
    val score: Float

    /**
     * A [Default] implementation of an [ScoredRetrievable].
     */
    @JvmRecord
    data class Default(
        override val id: UUID,
        override val transient: Boolean,
        override val partOf: Set<Retrievable>,
        override val parts: Set<Retrievable>,
        override val score: Float
    ): ScoredRetrievable
}