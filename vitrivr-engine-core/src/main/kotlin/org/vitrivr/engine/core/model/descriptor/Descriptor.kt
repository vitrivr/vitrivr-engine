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

    /** Indicator of what generated that descriptor */
    val sourceName: String?

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
}