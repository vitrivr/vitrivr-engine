package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.descriptor.FieldSchema
import org.vitrivr.engine.core.model.descriptor.FieldType
import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [StructDescriptor] used to store a string label and a confidence for that label.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class LabelDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId,
    val label: String,
    val confidence: Float,
    override val transient: Boolean = false
) : StructDescriptor {
    companion object {
        private val SCHEMA = listOf(
            FieldSchema("label", FieldType.STRING),
            FieldSchema("confidence", FieldType.FLOAT),
        )
    }

    /**
     * Returns the [FieldSchema] [List ]of this [LabelDescriptor].
     *
     * @return [List] of [FieldSchema]
     */
    override fun schema(): List<FieldSchema> = SCHEMA

    override fun values(): Map<String, Any?> = mapOf(
        "label" to this.label,
        "confidence" to this.confidence
    )
}
