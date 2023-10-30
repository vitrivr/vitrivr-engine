package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.Descriptor

/**
 * A [Descriptor] that uses a complex structure.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface StructDescriptor: Descriptor {
    /**
     * Returns the fields of this [StructDescriptor] as a [Map].
     *
     * @return A [Map] of this [StructDescriptor]'s fields without the IDs.
     */
    fun asMap(): Map<String, Any?>
}