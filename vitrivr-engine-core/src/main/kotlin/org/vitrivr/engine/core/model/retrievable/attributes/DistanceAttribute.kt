package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import kotlin.math.min

/**
 * A [MergingRetrievableAttribute] that contains a distance value.
 *
 * @author Luca Rossetto
 * @version 1.1.0
 */
data class DistanceAttribute(val distance: Float, val descriptorId: DescriptorId? = null) : MergingRetrievableAttribute {
    override fun merge(other: MergingRetrievableAttribute): DistanceAttribute = DistanceAttribute(
        min(this.distance, (other as? DistanceAttribute)?.distance ?: Float.POSITIVE_INFINITY)
    )
}
