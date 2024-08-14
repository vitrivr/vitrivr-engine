package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [Double] value.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
data class DoubleDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    override val value: Value.Double,
    override val field: Schema.Field<*, DoubleDescriptor>? = null
) : ScalarDescriptor<Value.Double> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Double))
    }

    /**
     * Returns the [Attribute] [List] of this [DoubleDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA

    /**
     * Returns a copy of this [DoubleDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [DoubleDescriptor].
     * @param retrievableId [RetrievableId] of the new [DoubleDescriptor].
     * @return Copy of this [DoubleDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = DoubleDescriptor(id, retrievableId, this.value, this.field)
}