package org.vitrivr.engine.core.model.database.retrievable

/**
 * A [Retrievable] that has been scored.
 *
 * Used as part of the query process
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithScore : Retrievable {
    /** The score of this [RetrievableWithScore]. Generally, a score must be between 0.0 and 1.0 */
    val score: Float
}