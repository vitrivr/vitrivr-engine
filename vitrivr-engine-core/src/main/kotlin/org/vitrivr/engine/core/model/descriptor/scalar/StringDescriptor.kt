package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [ScalarDescriptor] using a [String] value.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class StringDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    override val value: Value.String,
    override val field: Schema.Field<*, StringDescriptor>? = null
) : ScalarDescriptor<Value.String> {
    companion object {
        private val SCHEMA = listOf(Attribute("value", Type.String))
    }

    /**
     * Returns the [Attribute] [List ]of this [StringDescriptor].
     *
     * @return [List] of [Attribute]
     */
    override fun schema(): List<Attribute> = SCHEMA
}
