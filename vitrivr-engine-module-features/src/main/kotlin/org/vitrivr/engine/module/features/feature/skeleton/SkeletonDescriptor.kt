package org.vitrivr.engine.module.features.feature.skeleton

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A descriptor used to store a skeleton pose.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class SkeletonDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, SkeletonDescriptor>? = null
) : StructDescriptor<SkeletonDescriptor>(id, retrievableId, LAYOUT, values, field) {

    companion object {
        private val LAYOUT = listOf(
            Attribute("person", Type.Int),
            Attribute("skeleton", Type.FloatVector(12)),
            Attribute("weights", Type.FloatVector(12))
        )
    }

    /** The person index. */
    val person: Value.Int by this.values

    /** The vector describing the skeleton. */
    val skeleton: List<Value.Float> by this.values

    /** The vector describing the skeleto weights. */
    val weights: List<Value.Float> by this.values

    /**
     * Returns a copy of this [SkeletonDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [SkeletonDescriptor].
     * @param retrievableId [RetrievableId] of the new [SkeletonDescriptor].
     * @param field [Schema.Field] the new [SkeletonDescriptor] belongs to.
     * @return Copy of this [SkeletonDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, SkeletonDescriptor>?) = SkeletonDescriptor(id, retrievableId, this.values, field)
}