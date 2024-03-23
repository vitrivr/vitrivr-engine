package org.vitrivr.engine.core.util.math

import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/**
 * A collection of [ScoringFunctions] that can be used to score [Retrieved] object that come with a [DistanceAttribute].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
object ScoringFunctions {

    /**
     * A scoring functions that assumes a defined maximum and subtracts the distance from that maximum to obtain a score.
     *
     * @param retrieved [Retrieved] object to score.
     * @param max Maximum value. Default is 1.0.
     */
    fun max(retrieved: Retrieved, max: Float = 1.0f): ScoreAttribute {
        val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return ScoreAttribute.ZERO
        return ScoreAttribute(max - distance)
    }
}