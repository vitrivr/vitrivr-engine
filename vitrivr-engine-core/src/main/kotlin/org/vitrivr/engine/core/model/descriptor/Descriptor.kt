package org.vitrivr.engine.core.model.descriptor

import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
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
    var id: DescriptorId

    /** The [RetrievableId] of the [Retrievable] that is being described by this [Descriptor]. */
    var retrievableId: RetrievableId?

    /** The [Schema.Field] backing this [Descriptor]. */
    val field: Schema.Field<*, out Descriptor>?

    /** Flag indicating whether this [Descriptor] is persistent or not. */
    override val transient: Boolean
        get() = this.field != null

    /**
     * Returns a  [Attribute] [List] for this [Descriptor].
     *
     * @return [List] of [Attribute]
     */
    fun schema(): List<Attribute>

    /**
     * Returns the fields and its values of this [Descriptor] as a [Map].
     *
     * @return A [Map] of this [Descriptor]'s fields (without the IDs).
     */
    fun values(): Map<AttributeName, Value<*>?>
}