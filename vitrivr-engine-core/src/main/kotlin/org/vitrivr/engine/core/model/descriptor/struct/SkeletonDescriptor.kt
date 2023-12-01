package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId

data class SkeletonDescriptor(
        override val id: DescriptorId,
        override val retrievableId: RetrievableId,
        val person: Int,
        val skeleton: List<Float>,
        val weights: List<Float>,
        override val transient: Boolean = false
) : StructDescriptor {

    /**
     * Returns the [FieldSchema] [List ]of this [SkeletonDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = listOf(
            FieldSchema("person", FieldType.INT),
            FieldSchema("skeleton", FieldType.FLOAT, intArrayOf(this.skeleton.size)),
            FieldSchema("weights", FieldType.FLOAT, intArrayOf(this.weights.size))
    )

    override fun values(): List<Pair<String, Any?>> = listOf(
            "person" to this.person,
            "skeleton" to this.skeleton,
            "weights" to this.weights
    )
}