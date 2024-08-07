package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor.Companion.VALUE_ATTRIBUTE_NAME
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [String] value.
 *
 * Compared to [StringDescriptor]s, [TextDescriptor]s are typically longer and can be used to store larger
 * amounts of text. Certain databases use different storage mechanisms for [StringDescriptor]s and [TextDescriptor]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class TextDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    override val value: Value.Text,
    override val field: Schema.Field<*, TextDescriptor>? = null
) : ScalarDescriptor<Value.Text> {
    companion object {
        private val SCHEMA = listOf(Attribute(VALUE_ATTRIBUTE_NAME, Type.Text))
    }

    /**
     * Returns the [Attribute] [List ]of this [StringDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun layout(): List<Attribute> = SCHEMA
}