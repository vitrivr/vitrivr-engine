package org.vitrivr.engine.core.model.retrievable.attributes

import kotlin.math.min

data class DistanceAttribute(val distance: Float) : MergingRetrievableAttribute {
    override fun merge(other: MergingRetrievableAttribute): DistanceAttribute = DistanceAttribute(
        min(this.distance, (other as? DistanceAttribute)?.distance ?: Float.POSITIVE_INFINITY)
    )

}
