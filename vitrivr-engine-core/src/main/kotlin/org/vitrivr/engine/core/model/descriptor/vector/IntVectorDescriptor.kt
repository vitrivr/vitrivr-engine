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
 * A [VectorDescriptor] that uses a [Int] as elements.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */
data class IntVectorDescriptor(
    override val id: UUID = UUID.randomUUID(),
    override val retrievableId: RetrievableId? = null,
    override val vector: Value.IntVector,
    override val field: Schema.Field<*, IntVectorDescriptor>? = null
) : VectorDescriptor<Value.IntVector> {
    /**
     * Returns the [Attribute] [List ]of this [IntVectorDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = listOf(Attribute(VECTOR_ATTRIBUTE_NAME, Type.Int))

    /**
     * Returns a copy of this [IntVectorDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [IntVectorDescriptor].
     * @param retrievableId [RetrievableId] of the new [IntVectorDescriptor].
     * @return Copy of this [IntVectorDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = IntVectorDescriptor(id, retrievableId, Value.IntVector(this.vector.value.copyOf()), this.field)
}
