package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using a [Long] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class LongDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val transient: Boolean,
    override val value: Long,
): ScalarDescriptor<Long> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", FieldType.LONG))
    }

    /**
     * Returns the [FieldSchema] [List] of this [LongDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}