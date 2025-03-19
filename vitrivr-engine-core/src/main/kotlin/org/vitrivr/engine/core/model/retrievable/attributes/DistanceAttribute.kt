package org.vitrivr.engine.core.model.retrievable.attributes

import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.retrievable.Retrievable
import kotlin.math.min

/**
 * A [DistanceAttribute] that contains a distance value.
 *
 * @author Luca Rossetto
 * @version 1.1.0
 */
sealed interface DistanceAttribute: RetrievableAttribute {
    /** The distance value associated with this [DistanceAttribute]. */
    val distance: Double

    /**
     * A global [DistanceAttribute].
     *
     * It is used to store a global distance value for a [Retrievable].
     */
    data class Global(override val distance: Double): DistanceAttribute, MergingRetrievableAttribute {
        override fun merge(other: MergingRetrievableAttribute) = Global(
            min(this.distance, (other as? DistanceAttribute)?.distance ?: Double.POSITIVE_INFINITY)
        )
    }

    /**
     * A local [DistanceAttribute]. It is used to store a local distance value specific for a [Descriptor].
     */
    data class Local(override val distance: Double, val descriptorId: DescriptorId): DistanceAttribute
}
