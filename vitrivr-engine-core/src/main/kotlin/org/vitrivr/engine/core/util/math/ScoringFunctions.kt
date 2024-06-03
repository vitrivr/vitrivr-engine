package org.vitrivr.engine.core.util.math

import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import kotlin.math.sqrt

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
        val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return ScoreAttribute.Unbound(0.0f)
        return ScoreAttribute.Unbound(max - distance)
    }

    /**
     * A scoring function that assumes the distances to score are normalised on a hypersphere: `s = 1 / (d/sqrt(2))`.
     *
     * @param retrieved [Retrieved] object to score.
     */
    fun hypersphereNormalised(retrieved: Retrieved): ScoreAttribute{
        val distance = retrieved.filteredAttribute<DistanceAttribute>()?.distance ?: return ScoreAttribute.Unbound(0.0f)
        return ScoreAttribute.Unbound(1f-(distance / sqrt(2.0f)))
    }
}
