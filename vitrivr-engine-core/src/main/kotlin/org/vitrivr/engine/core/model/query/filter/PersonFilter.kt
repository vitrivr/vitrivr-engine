package org.vitrivr.engine.core.model.query.filter

import org.vitrivr.engine.core.model.descriptor.struct.FaceIdentityDescriptor

/** Recognized identities required on the same retrievable, independent of recording provenance. */
data class PersonFilter(
    val names: List<String>,
    val field: String = "yoloadafacetwopass",
    val mode: Mode = Mode.ALL,
    val minConfidence: Double? = null
) {
    enum class Mode { ALL, ANY }

    val normalizedNames = names.map { FaceIdentityDescriptor.normalizeLabel(it.trim()) }.distinct()

    init {
        require(field.matches(Regex("[A-Za-z][A-Za-z0-9_]*"))) { "Invalid identity filter field." }
        require(normalizedNames.isNotEmpty() && normalizedNames.all { it.isNotBlank() && it.length <= 255 }) {
            "Person filters require non-blank labels of at most 255 characters."
        }
        require(minConfidence == null || (minConfidence.isFinite() && minConfidence in 0.0..1.0)) {
            "filter.personMinConfidence must be between 0 and 1."
        }
    }
}
