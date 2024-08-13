package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [StructDescriptor] used to store a string label and a confidence for that label.
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
class LabelDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, LabelDescriptor>? = null
) : MapStructDescriptor(id, retrievableId, SCHEMA, values, field) {
    companion object {
        private val SCHEMA = listOf(
            Attribute("label", Type.String),
            Attribute("confidence", Type.Float),
        )
    }

    /** The stored label. */
    val label: Value.String by this.values

    /** The associated confidence. */
    val confidence: Value.Float by this.values
}
