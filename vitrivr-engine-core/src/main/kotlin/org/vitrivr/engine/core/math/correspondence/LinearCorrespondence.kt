package org.vitrivr.engine.core.math.correspondence

import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/**
 * A linear [CorrespondenceFunction] that is based on a maximum distance.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class LinearCorrespondence(private val maximumDistance: Float) : CorrespondenceFunction {
    override fun invoke(distance: DistanceAttribute): ScoreAttribute.Similarity = ScoreAttribute.Similarity(1.0f - (distance.distance / this.maximumDistance))
}