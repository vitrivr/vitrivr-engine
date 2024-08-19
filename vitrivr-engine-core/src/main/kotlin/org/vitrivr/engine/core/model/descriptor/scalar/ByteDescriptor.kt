package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using an [Value.Byte].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class ByteDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    override val value: Value.Byte,
    override val field: Schema.Field<*, ByteDescriptor>? = null
) : ScalarDescriptor<ByteDescriptor, Value.Byte> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Byte))
    }

    /**
     * Returns the [Attribute] [List] of this [IntDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA

    /**
     * Returns a copy of this [ByteDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [ByteDescriptor].
     * @param retrievableId [RetrievableId] of the new [ByteDescriptor].
     * @param field [Schema.Field] the new [ByteDescriptor] belongs to.
     * @return Copy of this [ByteDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, ByteDescriptor>?) = ByteDescriptor(id, retrievableId, this.value, field)
}