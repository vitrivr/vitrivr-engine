package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [Float] value.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
data class FloatDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    override val value: Value.Float,
    override val field: Schema.Field<*, FloatDescriptor>? = null
) : ScalarDescriptor<Value.Float> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Float))
    }

    /**
     * Returns the [Attribute] [List] of this [FloatDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA

    /**
     * Returns a copy of this [FloatDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [FloatDescriptor].
     * @param retrievableId [RetrievableId] of the new [FloatDescriptor].
     * @return Copy of this [FloatDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = FloatDescriptor(id, retrievableId, this.value, this.field)
}