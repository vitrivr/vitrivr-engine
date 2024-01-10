package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using a [Float] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class FloatDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val value: Float,
    override val transient: Boolean = false
): ScalarDescriptor<Float> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", FieldType.FLOAT))
    }

    /**
     * Returns the [FieldSchema] [List] of this [FloatDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}