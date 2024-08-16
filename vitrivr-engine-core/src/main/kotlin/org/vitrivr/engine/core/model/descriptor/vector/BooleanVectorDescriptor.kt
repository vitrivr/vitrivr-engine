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
 * A [VectorDescriptor] that uses a [Boolean] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */

data class BooleanVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: Value.BooleanVector,
    override val field: Schema.Field<*, BooleanVectorDescriptor>? = null
) : VectorDescriptor<BooleanVectorDescriptor, Value.BooleanVector> {
    /**
     * Returns the [Attribute] [List ]of this [BooleanVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = listOf(Attribute(VECTOR_ATTRIBUTE_NAME, Type.BooleanVector(this.dimensionality)))

    /**
     * Returns a copy of this [BooleanVectorDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [BooleanVectorDescriptor].
     * @param retrievableId [RetrievableId] of the new [BooleanVectorDescriptor].
     * @param field [Schema.Field] the new [BooleanVectorDescriptor] belongs to.
     * @return Copy of this [BooleanVectorDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, BooleanVectorDescriptor>?) = BooleanVectorDescriptor(id, retrievableId, Value.BooleanVector(this.vector.value.copyOf()), field)
}
