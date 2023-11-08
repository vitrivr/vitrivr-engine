package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor

/**
 * A [Descriptor] that uses a [List] (vector) of values [T] of some sort.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface VectorDescriptor<T> : Descriptor {
    /** The size of this [VectorDescriptor]. */
    val dimensionality: Int
        get() = this.vector.size

    /** The [List] of values [T]. */
    val vector: List<T>

    /**
     * Returns the fields and its values of this [ScalarDescriptor] as a [Map].
     *
     * @return A [Map] of this [ScalarDescriptor]'s fields (without the IDs).
     */
    override fun values(): List<Pair<String, List<T>?>> = listOf("vector" to this.vector)
}