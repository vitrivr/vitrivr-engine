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
 * A [VectorDescriptor] that uses a [Double] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */

data class DoubleVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: Value.DoubleVector,
    override val field: Schema.Field<*, DoubleVectorDescriptor>? = null
) : VectorDescriptor<Value.DoubleVector> {
    /**
     * Returns the [Attribute] [List ]of this [DoubleVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = listOf(Attribute(VECTOR_ATTRIBUTE_NAME, Type.DoubleVector(this.dimensionality)))

    /**
     * Returns a copy of this [DoubleVectorDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [DoubleVectorDescriptor].
     * @param retrievableId [RetrievableId] of the new [DoubleVectorDescriptor].
     * @return Copy of this [DoubleVectorDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = DoubleVectorDescriptor(id, retrievableId, Value.DoubleVector(this.vector.value.copyOf()), this.field)
}
