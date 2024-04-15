package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [Long] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class LongDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val value: Value.Long,
    override val field: Schema.Field<*, LongDescriptor>? = null
) : ScalarDescriptor<Value.Long> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", Type.LONG))
    }

    /**
     * Returns the [FieldSchema] [List] of this [LongDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}