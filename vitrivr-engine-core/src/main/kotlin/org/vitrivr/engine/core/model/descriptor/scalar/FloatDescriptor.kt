package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [Float] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class FloatDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    override val value: Value.Float,
    override val field: Schema.Field<*, FloatDescriptor>? = null
) : ScalarDescriptor<Value.Float> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", Type.FLOAT))
    }

    /**
     * Returns the [FieldSchema] [List] of this [FloatDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}