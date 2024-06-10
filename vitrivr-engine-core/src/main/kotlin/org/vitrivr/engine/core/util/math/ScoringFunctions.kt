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
     * A scoring function that assumes a defined minimum and maximum and normalizes the distance between these two values.
     *
     * @param retrieved [Retrieved] object to score.
     * @param min Minimum value. Default is 0.0.
     * @param max Maximum value. Default is 1.0.
     */
    fun bounded(retrieved: Retrieved, min: Float = 0.0f, max: Float = 1.0f): ScoreAttribute {
        val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return ScoreAttribute.Similarity(0.0f)
        return ScoreAttribute.Similarity((max-distance) / (max - min))
    }

    /**
     * A scoring functions that assumes a defined maximum and subtracts the distance from that maximum to obtain a score.
     *
     * @param retrieved [Retrieved] object to score.
     * @param max Maximum value. Default is 1.0.
     */
    fun max(retrieved: Retrieved, max: Float = 1.0f): ScoreAttribute {
        val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return ScoreAttribute.Unbound(0.0f)
        return ScoreAttribute.Unbound(max - distance)
    }
}