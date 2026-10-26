package org.vitrivr.engine.core.model.query.filter

import org.vitrivr.engine.core.model.query.Query

/** Database-side recording and identity restrictions, combined with AND before result limits. */
data class MetadataFilter(
    val field: String = "recording",
    val values: Map<String, List<String>> = emptyMap(),
    val persons: PersonFilter? = null
) {
    init {
        require(field.matches(Regex("[A-Za-z][A-Za-z0-9_]*"))) { "Invalid metadata filter field." }
        require(values.isNotEmpty() || persons != null) { "An early filter needs at least one recording or person constraint." }
        require(values.keys.all { it in ATTRIBUTES }) { "Supported recording filters: $ATTRIBUTES" }
        require(values.values.all { it.isNotEmpty() && it.all { value -> value.isNotBlank() && value.length <= 255 } }) {
            "Filter values must be non-blank strings of at most 255 characters."
        }
        require(values["source_type"]?.all { it in setOf("person", "room", "unknown") } != false) {
            "source_type must be person, room or unknown."
        }
    }

    companion object {
        private val ATTRIBUTES = setOf("day", "source", "source_type")
        private val PERSON_KEYS = setOf("filter.persons", "filter.personMode", "filter.identityField", "filter.personMinConfidence")

        fun fromParameters(parameters: Map<String, String>): MetadataFilter? {
            val filters = parameters.filterKeys { it.startsWith("filter.") }
            if (filters.isEmpty()) return null
            require(filters.keys.all { it == "filter.field" || it in PERSON_KEYS || it.removePrefix("filter.") in ATTRIBUTES }) {
                "Unknown early filter parameter."
            }
            val personParameters = filters.filterKeys { it in PERSON_KEYS }
            require(personParameters.isEmpty() || "filter.persons" in personParameters) {
                "filter.persons is required with identity filter options."
            }
            val persons = filters["filter.persons"]?.let { names ->
                val mode = when (filters["filter.personMode"]?.trim()?.lowercase() ?: "all") {
                    "all" -> PersonFilter.Mode.ALL
                    "any" -> PersonFilter.Mode.ANY
                    else -> throw IllegalArgumentException("filter.personMode must be all or any.")
                }
                val confidence = filters["filter.personMinConfidence"]?.let {
                    it.toDoubleOrNull() ?: throw IllegalArgumentException("filter.personMinConfidence must be a number.")
                }
                PersonFilter(names.split(','), filters["filter.identityField"] ?: "yoloadafacetwopass", mode, confidence)
            }
            val values = filters.filterKeys { it.removePrefix("filter.") in ATTRIBUTES }
                .mapKeys { it.key.removePrefix("filter.") }
                .mapValues { (_, value) -> value.split(',').map { it.trim() }.distinct() }
            return MetadataFilter(filters["filter.field"] ?: "recording", values, persons)
        }
    }
}

/** Backends must implement this restriction or reject it; never fall back to late filtering. */
data class MetadataFilteredQuery(val query: Query, val filter: MetadataFilter) : Query
