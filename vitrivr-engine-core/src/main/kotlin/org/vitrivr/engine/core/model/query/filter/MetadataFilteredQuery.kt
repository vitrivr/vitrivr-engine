package org.vitrivr.engine.core.model.query.filter

import org.vitrivr.engine.core.model.query.Query

/** Database-side restriction: AND across attributes, OR across each attribute's values. */
data class MetadataFilter(val field: String, val values: Map<String, List<String>>) {
    init {
        require(field.matches(Regex("[A-Za-z][A-Za-z0-9_]*"))) { "Invalid metadata filter field." }
        require(values.isNotEmpty()) { "A metadata filter needs at least one constraint." }
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

        fun fromParameters(parameters: Map<String, String>): MetadataFilter? {
            val filters = parameters.filterKeys { it.startsWith("filter.") }
            if (filters.isEmpty()) return null
            require(filters.keys.all { it == "filter.field" || it.removePrefix("filter.") in ATTRIBUTES }) {
                "Unknown recording filter parameter."
            }
            val values = filters.filterKeys { it != "filter.field" }.mapKeys { it.key.removePrefix("filter.") }
                .mapValues { (_, value) -> value.split(',').map { it.trim() }.distinct() }
            return MetadataFilter(filters["filter.field"] ?: "recording", values)
        }
    }
}

/** Backends must implement this restriction or reject it; never fall back to late filtering. */
data class MetadataFilteredQuery(val query: Query, val filter: MetadataFilter) : Query
