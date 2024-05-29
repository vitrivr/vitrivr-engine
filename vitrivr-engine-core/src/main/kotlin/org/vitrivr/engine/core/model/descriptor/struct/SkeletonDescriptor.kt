package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

data class SkeletonDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    val person: Value.Int,
    val skeleton: List<Value.Float>,
    val weights: List<Value.Float>,
    override val field: Schema.Field<*, SkeletonDescriptor>? = null
) : StructDescriptor {

    /**
     * Returns the [FieldSchema] [List ]of this [SkeletonDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = listOf(
        FieldSchema("person", Type.INT),
        FieldSchema("skeleton", Type.FLOAT, intArrayOf(this.skeleton.size)),
        FieldSchema("weights", Type.FLOAT, intArrayOf(this.weights.size))
    )

    override fun values(): List<Pair<String, Any?>> = listOf(
        "person" to this.person,
        "skeleton" to this.skeleton,
        "weights" to this.weights
    )
}