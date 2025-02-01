package org.vitrivr.engine.core.math.correspondence

import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/**
 * A [CorrespondenceFunction] is used to compute the [ScoreAttribute.Similarity] for a given [DistanceAttribute].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface CorrespondenceFunction {
    /**
     * Computes the score for a given [DistanceAttribute].
     *
     * @param distance [DistanceAttribute] for which to compute the score.
     * @return [ScoreAttribute] for the given [DistanceAttribute].
     */
    operator fun invoke(distance: DistanceAttribute): ScoreAttribute
}