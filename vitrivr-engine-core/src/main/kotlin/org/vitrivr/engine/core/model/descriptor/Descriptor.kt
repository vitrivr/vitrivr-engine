package org.vitrivr.engine.core.model.descriptor

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.serializer.UUIDSerializer
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/** A typealias to identify the [UUID] identifying a [Descriptor]. */
typealias DescriptorId = @Serializable(UUIDSerializer::class) UUID

/**
 * A [Persistable] [Descriptor] that can be used to describe a [Retrievable].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Descriptor<T : Descriptor<T>> : Persistable {
    /** The [DescriptorId] held by this [Descriptor]. */
    val id: DescriptorId

    /** The [RetrievableId] of the [Retrievable] that is being described by this [Descriptor]. */
    val retrievableId: RetrievableId?

    /** The [Schema.Field] backing this [Descriptor]. */
    val field: Schema.Field<*, out T>?

    /** Flag indicating whether this [Descriptor] is persistent or not. */
    override val transient: Boolean
        get() = this.field != null

    /**
     * Returns a  [Attribute] [List] for this [Descriptor].
     *
     * @return [List] of [Attribute]
     */
    fun layout(): List<Attribute>

    /**
     * Returns the fields and its values of this [Descriptor] as a [Map].
     *
     * @return A [Map] of this [Descriptor]'s fields (without the IDs).
     */
    fun values(): Map<AttributeName, Value<*>?>

    /**
     * Returns a copy of this [Descriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [Descriptor].
     * @param retrievableId [RetrievableId] of the new [Descriptor].
     * @param field [Schema.Field] the new [Descriptor] belongs to.
     *
     * @return Copy of this [Descriptor].
     */
    fun copy(id: DescriptorId = this.id, retrievableId: RetrievableId? = this.retrievableId, field: Schema.Field<*, T>? = null): T
}