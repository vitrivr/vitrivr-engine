package org.vitrivr.engine.core.math.correspondence

import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute

/**
 * A [CorrespondenceFunction] that is based on and upper and lower bound for the distance value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BoundedCorrespondence(private val min: Float = 0.0f, private val max: Float = 1.0f) : CorrespondenceFunction {
    override fun invoke(distance: DistanceAttribute): ScoreAttribute.Similarity = ScoreAttribute.Similarity((this.max - distance.distance) / (this.max - this.min))
}