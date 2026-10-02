package org.vitrivr.engine.core.model.descriptor.struct

import org.vitrivr.engine.core.model.descriptor.Attribute
import org.vitrivr.engine.core.model.descriptor.AttributeName
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import java.text.Normalizer

/**
 * A face identity with a label, confidence, and optional 512-dimensional AdaFace embedding.

 */
class FaceIdentityDescriptor(
    override val id: DescriptorId,
    override val retrievableId: RetrievableId?,
    values: Map<AttributeName, Value<*>?>,
    override val field: Schema.Field<*, FaceIdentityDescriptor>? = null
) : StructDescriptor<FaceIdentityDescriptor>(id, retrievableId, SCHEMA, values.mapValues { (name, value) ->
    if (name == LABEL_FIELD_NAME && value is Value.String) Value.String(normalizeLabel(value.value)) else value
}, field) {
    companion object {

        const val LABEL_FIELD_NAME = "label"
        const val CONFIDENCE_FIELD_NAME = "confidence"
        const val VECTOR_FIELD_NAME = "vector"
        const val DIMENSIONS = 512

        /** Canonical form shared by stored labels and query labels; preserves case and accents. */
        fun normalizeLabel(label: String): String = Normalizer.normalize(label, Normalizer.Form.NFC)

        private val SCHEMA = listOf(
            Attribute(LABEL_FIELD_NAME, Type.String),
            Attribute(CONFIDENCE_FIELD_NAME, Type.Float),
            Attribute(VECTOR_FIELD_NAME, Type.FloatVector(DIMENSIONS), nullable = true),
        )
    }

    constructor(
        id: DescriptorId,
        retrievableId: RetrievableId?,
        label: String,
        confidence: Float = 1f,
        vector: Value.FloatVector? = null,
        field: Schema.Field<*, FaceIdentityDescriptor>? = null
    ) : this(id, retrievableId, mapOf(LABEL_FIELD_NAME to Value.String(label), CONFIDENCE_FIELD_NAME to Value.Float(confidence), VECTOR_FIELD_NAME to vector), field)

    init {
        val vector = values[VECTOR_FIELD_NAME]
        require(vector == null || (vector is Value.FloatVector && vector.value.size == DIMENSIONS && vector.value.all { it.isFinite() })) {
            "Face embedding must contain $DIMENSIONS finite floats."
        }
    }

    /** Null when no face was detected (for example, a body-only identity). */
    val vector: Value.FloatVector? get() = values[VECTOR_FIELD_NAME] as? Value.FloatVector

    /** The stored label. */
    val label: Value.String by this.values

    /** The associated confidence. */
    val confidence: Value.Float by this.values

    /**
     * Returns a copy of this [FaceIdentityDescriptor] with new [RetrievableId] and/or [DescriptorId]
     *
     * @param id [DescriptorId] of the new [FaceIdentityDescriptor].
     * @param retrievableId [RetrievableId] of the new [FaceIdentityDescriptor].
     * @param field [Schema.Field] the new [FaceIdentityDescriptor] belongs to.
     * @return Copy of this [FaceIdentityDescriptor].
     */
    override fun copy(id: DescriptorId, retrievableId: RetrievableId?, field: Schema.Field<*, FaceIdentityDescriptor>?) =
        FaceIdentityDescriptor(id, retrievableId, HashMap(this.values).also { values -> values[VECTOR_FIELD_NAME] = vector?.let { Value.FloatVector(it.value.copyOf()) } }, field)
}
