package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [Boolean] value.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */

data class BooleanDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    override val value: Value.Boolean,
    override val field: Schema.Field<*, BooleanDescriptor>? = null
) : ScalarDescriptor<Value.Boolean> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Boolean))
    }

    /**
     * Returns the [Attribute] [List ]of this [BooleanDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA

    /**
     * Returns a copy of this [BooleanDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [BooleanDescriptor].
     * @param retrievableId [RetrievableId] of the new [BooleanDescriptor].
     * @return Copy of this [BooleanDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = BooleanDescriptor(id, retrievableId, this.value, this.field)
}