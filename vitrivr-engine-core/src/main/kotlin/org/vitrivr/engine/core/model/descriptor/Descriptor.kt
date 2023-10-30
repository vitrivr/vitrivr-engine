package org.vitrivr.engine.core.model.descriptor

import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import java.util.*

/** A typealias to identify the [UUID] identifying a [Descriptor]. */
typealias DescriptorId = UUID

/**
 * A [Persistable] [Descriptor] that can be used to describe a [Retrievable].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Descriptor : Persistable {
    /** The [DescriptorId] held by this [Descriptor]. */
    override val id: DescriptorId

    /** The [RetrievableId] of the [Retrievable] that is being described by this [Descriptor]. */
    val retrievableId: RetrievableId?

    /**
     * Returns a  [FieldSchema] [List] for this [Descriptor].
     *
     * @return [List] of [FieldSchema]
     */
    fun schema(): List<FieldSchema>

    /**
     * Returns the fields and its values of this [Descriptor] as a [Map].
     *
     * @return A [Map] of this [Descriptor]'s fields (without the IDs).
     */
    fun values(): Map<String, Any?>
}