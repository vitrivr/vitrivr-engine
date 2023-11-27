package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Descriptor

/**
 * A [Descriptor] with a scalar value [T].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface ScalarDescriptor<T> : Descriptor {

    /** The [Value] held by this [ScalarDescriptor]. */
    val value: T

    /**
     * Returns the fields and its values of this [ScalarDescriptor] as a [Map].
     *
     * @return A [Map] of this [ScalarDescriptor]'s fields (without the IDs).
     */
    override fun values(): List<Pair<String, T?>> = listOf("value" to this.value)
}
