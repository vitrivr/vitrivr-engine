package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [Double] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class DoubleDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    override val value: Value.Double,
    override val field: Schema.Field<*, DoubleDescriptor>? = null
) : ScalarDescriptor<Value.Double> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", Type.DOUBLE))
    }

    /**
     * Returns the [FieldSchema] [List] of this [DoubleDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}