package org.vitrivr.engine.core.model.descriptor.scalar


import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [Long] value.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
data class LongDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    override val value: Value.Long,
    override val field: Schema.Field<*, LongDescriptor>? = null
) : ScalarDescriptor<Value.Long> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Long))
    }

    /**
     * Returns the [Attribute] [List] of this [LongDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA

    /**
     * Returns a copy of this [LongDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [LongDescriptor].
     * @param retrievableId [RetrievableId] of the new [LongDescriptor].
     * @return Copy of this [LongDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = LongDescriptor(id, retrievableId, this.value, this.field)
}