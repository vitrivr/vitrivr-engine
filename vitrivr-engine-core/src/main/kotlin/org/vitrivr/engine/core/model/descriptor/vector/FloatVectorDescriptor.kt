package org.vitrivr.engine.core.model.descriptor.vector

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor.Companion.VECTOR_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A [VectorDescriptor] that uses a [FloatArray].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */
data class FloatVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: Value.FloatVector,
    override val field: Schema.Field<*, FloatVectorDescriptor>? = null
) : VectorDescriptor<FloatVectorDescriptor, Value.FloatVector> {
    /**
     * Returns the [Attribute] [List ]of this [FloatVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = listOf(Attribute(VECTOR_ATTRIBUTE_NAME, Type.FloatVector(this.dimensionality)))

    /**
     * Returns a copy of this [FloatVectorDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [FloatVectorDescriptor].
     * @param retrievableId [RetrievableId] of the new [FloatVectorDescriptor].
     * @param field [Schema.Field] the new [DoubleVectorDescriptor] belongs to.
     * @return Copy of this [FloatVectorDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, FloatVectorDescriptor>?) = FloatVectorDescriptor(id, retrievableId, Value.FloatVector(this.vector.value.copyOf()), field)
}
