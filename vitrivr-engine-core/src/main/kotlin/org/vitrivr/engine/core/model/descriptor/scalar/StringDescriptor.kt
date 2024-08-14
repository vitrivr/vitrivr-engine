package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [String] value.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */

data class StringDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    override val value: Value.String,
    override val field: Schema.Field<*, StringDescriptor>? = null
) : ScalarDescriptor<Value.String> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.String))
    }

    /**
     * Returns the [Attribute] [List ]of this [StringDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA

    /**
     * Returns a copy of this [StringDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [StringDescriptor].
     * @param retrievableId [RetrievableId] of the new [StringDescriptor].
     * @return Copy of this [StringDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = StringDescriptor(id, retrievableId, this.value, this.field)
}
