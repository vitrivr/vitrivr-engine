package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value

/**
 * An abstract [MapStructDescriptor] implementation that can be configured to store any type of [Value] in its fields.
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
class AnyMapStructDescriptor(
    id: DescriptorId,
    retrievableId: RetrievableId?,
    layout: List<Attribute>,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, AnyMapStructDescriptor>? = null
) : MapStructDescriptor<AnyMapStructDescriptor>(id, retrievableId, layout, values, field) {

    /**
     * Returns a copy of this [MapStructDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [AnyMapStructDescriptor].
     * @param retrievableId [RetrievableId] of the new [AnyMapStructDescriptor].
     * @param field [Schema.Field] the new [AnyMapStructDescriptor] belongs to.
     * @return Copy of this [AnyMapStructDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, AnyMapStructDescriptor>?) = AnyMapStructDescriptor(id, retrievableId, this.layout, HashMap(this.values), field)
}