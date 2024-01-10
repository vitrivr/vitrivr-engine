package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [ScalarDescriptor] using a [Double] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class DoubleDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val value: Double,
    override val transient: Boolean = false
): ScalarDescriptor<Double> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", FieldType.DOUBLE))
    }

    /**
     * Returns the [FieldSchema] [List] of this [DoubleDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}