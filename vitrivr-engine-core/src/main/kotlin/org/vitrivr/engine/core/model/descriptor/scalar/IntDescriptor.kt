package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using an [Int] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class IntDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    override val value: Value.Int,
    override val field: Schema.Field<*, IntDescriptor>? = null
) : ScalarDescriptor<Value.Int> {
    companion object {
        private val SCHEMA = listOf(FieldSchema("value", Type.INT))
    }

    /**
     * Returns the [FieldSchema] [List] of this [IntDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA
}