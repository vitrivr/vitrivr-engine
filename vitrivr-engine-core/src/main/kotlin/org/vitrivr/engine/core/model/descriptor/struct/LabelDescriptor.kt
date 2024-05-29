package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value

/**
 * A [StructDescriptor] used to store a string label and a confidence for that label.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class LabelDescriptor(
    override var id: DescriptorId,
    override var retrievableId: RetrievableId?,
    val label: Value.String,
    val confidence: Value.Float,
    override val field: Schema.Field<*, LabelDescriptor>? = null
) : StructDescriptor {
    companion object {
        private val SCHEMA = listOf(
            FieldSchema("label", Type.STRING),
            FieldSchema("confidence", Type.FLOAT),
        )
    }

    /**
     * Returns the [FieldSchema] [List ]of this [LabelDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    override fun values(): List<Pair<String, Any?>> = listOf(
        "label" to this.label,
        "confidence" to this.confidence
    )
}
