package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using an [Int] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class IntDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val value: Int,
    override val transient: Boolean = false
): ScalarDescriptor<Int> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", FieldType.INT))
    }

    /**
     * Returns the [FieldSchema] [List] of this [IntDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}