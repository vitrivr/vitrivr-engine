package org.vitrivr.engine.core.model.descriptor.scalar

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.types.Value

/**
 * A [Descriptor] with a scalar value [T].
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
@Serializable
sealed interface ScalarDescriptor<T : Value.ScalarValue<*>> : Descriptor {

    companion object {
        const val VALUE_ATTRIBUTE_NAME = "value"
    }

    /** The [Value] held by this [ScalarDescriptor]. */
    val value: T

    /**
     * Returns the fields and its values of this [ScalarDescriptor] as a [Map].
     *
     * @return A [Map] of this [ScalarDescriptor]'s fields (without the IDs).
     */
    override fun values(): Map<AttributeName, T?> = mapOf("value" to this.value)
}
