package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
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
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, LabelDescriptor>? = null
) : StructDescriptor<LabelDescriptor>(id, retrievableId, SCHEMA, values, field) {
    companion object {

        const val LABEL_FIELD_NAME = "label"
        const val CONFIDENCE_FIELD_NAME = "label"

        private val SCHEMA = listOf(
            Attribute(LABEL_FIELD_NAME, Type.String),
            Attribute(CONFIDENCE_FIELD_NAME, Type.Float),
        )
    }

    constructor(
        id: DescriptorId,
        retrievableId: RetrievableId?,
        label: String,
        confidence: Float = 1f,
        field: Schema.Field<*, LabelDescriptor>? = null
    ) : this(id, retrievableId, mapOf(LABEL_FIELD_NAME to Value.String(label), CONFIDENCE_FIELD_NAME to Value.Float(confidence)), field)

    /** The stored label. */
    val label: Value.String by this.values

    /** The associated confidence. */
    val confidence: Value.Float by this.values

    /**
     * Returns a copy of this [LabelDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [LabelDescriptor].
     * @param retrievableId [RetrievableId] of the new [LabelDescriptor].
     * @param field [Schema.Field] the new [LabelDescriptor] belongs to.
     * @return Copy of this [LabelDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, LabelDescriptor>?) =
        LabelDescriptor(id, retrievableId, HashMap(this.values), this.field)
}
