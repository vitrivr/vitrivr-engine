package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.types.Value

/**
 * A [Descriptor] with a scalar value [V].
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
sealed interface ScalarDescriptor<T : ScalarDescriptor<T, V>, V : Value.ScalarValue<*>> : Descriptor<T> {

    companion object {
        const val VALUE_ATTRIBUTE_NAME = "value"
        const val VALUE_INDEX_NAME = "search_vector"
    }

    /** The [Value] held by this [ScalarDescriptor]. */
    val value: V

    /**
     * Returns the fields and its values of this [ScalarDescriptor] as a [Map].
     *
     * @return A [Map] of this [ScalarDescriptor]'s fields (without the IDs).
     */
    override fun values(): Map<AttributeName, V?> = mapOf("value" to this.value)
}
