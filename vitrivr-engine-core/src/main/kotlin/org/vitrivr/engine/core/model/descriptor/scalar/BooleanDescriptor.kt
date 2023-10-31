package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.descriptor.struct.StructDescriptor
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using a [Boolean] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class BooleanDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val transient: Boolean,
    override val value: Boolean,
): ScalarDescriptor<Boolean> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", FieldType.BOOLEAN))
    }

    /**
     * Returns the [FieldSchema] [List ]of this [BooleanDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}