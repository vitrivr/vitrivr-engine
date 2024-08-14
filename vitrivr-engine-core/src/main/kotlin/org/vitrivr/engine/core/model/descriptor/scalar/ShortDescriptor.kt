package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using an [Value.Short].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class ShortDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    override val value: Value.Short,
    override val field: Schema.Field<*, IntDescriptor>? = null
) : ScalarDescriptor<Value.Short> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Short))
    }

    /**
     * Returns the [Attribute] [List] of this [IntDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA

    /**
     * Returns a copy of this [ShortDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [ShortDescriptor].
     * @param retrievableId [RetrievableId] of the new [ShortDescriptor].
     * @return Copy of this [ShortDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?) = ShortDescriptor(id, retrievableId, this.value, this.field)
}