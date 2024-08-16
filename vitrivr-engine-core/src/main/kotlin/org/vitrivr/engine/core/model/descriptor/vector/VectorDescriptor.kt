package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.types.Value

/**
 * A [Descriptor] that uses a [List] (vector) of values [V] of some sort.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
sealed interface VectorDescriptor<T : VectorDescriptor<T, V>, V : Value.Vector<*>> : Descriptor<T> {
    companion object {
        const val VECTOR_ATTRIBUTE_NAME = "vector"
    }

    /** The size of this [VectorDescriptor]. */
    val dimensionality: Int
        get() = this.vector.size

    /** The [List] of values [V]. */
    val vector: V

    /**
     * Returns the fields and its values of this [ScalarDescriptor] as a [Map].
     *
     * @return A [Map] of this [ScalarDescriptor]'s fields (without the IDs).
     */
    override fun values(): Map<AttributeName, V?> = mapOf(VECTOR_ATTRIBUTE_NAME to this.vector)
}