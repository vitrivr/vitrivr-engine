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
 * A [VectorDescriptor] that uses a [Long] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */

data class LongVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: Value.LongVector,
    override val field: Schema.Field<*, LongVectorDescriptor>? = null
) : VectorDescriptor<LongVectorDescriptor, Value.LongVector> {
    /**
     * Returns the [Attribute] [List ]of this [LongVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = listOf(Attribute(VECTOR_ATTRIBUTE_NAME, Type.Long))

    /**
     * Returns a copy of this [LongVectorDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [LongVectorDescriptor].
     * @param retrievableId [RetrievableId] of the new [LongVectorDescriptor].
     * @param field [Schema.Field] the new [LongVectorDescriptor] belongs to.
     * @return Copy of this [LongVectorDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, LongVectorDescriptor>?) = LongVectorDescriptor(id, retrievableId, Value.LongVector(this.vector.value.copyOf()), field)
}
