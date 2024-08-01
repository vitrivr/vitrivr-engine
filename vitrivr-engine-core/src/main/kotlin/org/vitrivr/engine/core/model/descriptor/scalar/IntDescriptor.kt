package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
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
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    override val value: Value.Int,
    override val field: Schema.Field<*, IntDescriptor>? = null,
    override val sourceName: String? = null
) : ScalarDescriptor<Value.Int> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Int))
    }

    /**
     * Returns the [Attribute] [List] of this [IntDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA
}