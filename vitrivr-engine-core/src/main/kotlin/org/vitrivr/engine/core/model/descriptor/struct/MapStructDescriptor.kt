package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.VideoSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value

/**
 * An abstract [StructDescriptor] implementation that is backed by a [Map] of [Value]s.
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
open class MapStructDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    protected val layout: List<Attribute>,
    protected val values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, out StructDescriptor>? = null
) : StructDescriptor {
    /**
     * Returns a  [Attribute] [List] for this [Descriptor].
     *
     * @return [List] of [Attribute]
     */
    final override fun layout(): List<Attribute> = this.layout

    /**
     * Returns the fields and its values of this [Descriptor] as a [Map].
     *
     * @return A [Map] of this [Descriptor]'s fields (without the IDs).
     */
    final override fun values(): Map<AttributeName, Value<*>?> = this.values

    /**
     * Returns a copy of this [VideoSourceMetadataDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [VideoSourceMetadataDescriptor].
     * @param retrievableId [RetrievableId] of the new [VideoSourceMetadataDescriptor].
     * @return Copy of this [VideoSourceMetadataDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = MapStructDescriptor(id, retrievableId, this.layout, HashMap(this.values), this.field)
}