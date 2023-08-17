package org.vitrivr.engine.core.model.database.descriptor.vector

import org.vitrivr.engine.core.model.database.descriptor.Descriptor

/**
 * A [Descriptor] that uses a [List] (vector) of values [T] of some sort.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface VectorDescriptor<T> : Descriptor {
    /** The size of this [VectorDescriptor]. */
    val dimensionality: Int
        get() = this.vector.size

    /** The [List] of values [T]. */
    val vector: List<T>
}