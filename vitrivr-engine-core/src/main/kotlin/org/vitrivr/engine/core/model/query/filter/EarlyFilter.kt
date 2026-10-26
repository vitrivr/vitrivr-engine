package org.vitrivr.engine.core.model.query.filter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Boolean eligibility on the same retrievable ID, evaluated before ranking/limits. */
@Serializable
sealed interface EarlyFilter {
    @Serializable
    @SerialName("all")
    data class All(val conditions: List<EarlyFilter>) : EarlyFilter {
        init { require(conditions.isNotEmpty()) { "all requires at least one condition." } }
    }

    @Serializable
    @SerialName("any")
    data class Any(val conditions: List<EarlyFilter>) : EarlyFilter {
        init { require(conditions.isNotEmpty()) { "any requires at least one condition." } }
    }

    @Serializable
    @SerialName("not")
    data class Not(val condition: EarlyFilter) : EarlyFilter

    @Serializable
    @SerialName("persons")
    data class Persons(
        val names: List<String>,
        val field: String = "yoloadafacetwopass",
        val mode: PersonFilter.Mode = PersonFilter.Mode.ALL,
        val minConfidence: Double? = null
    ) : EarlyFilter {
        init { PersonFilter(names, field, mode, minConfidence) }
    }

    @Serializable
    @SerialName("recording")
    data class Recording(val values: Map<String, List<String>>, val field: String = "recording") : EarlyFilter {
        init { MetadataFilter(field, values) }
    }

    @Serializable
    enum class TextMatch {
        @SerialName("fulltext") FULLTEXT,
        @SerialName("contains") CONTAINS,
        @SerialName("exact") EXACT
    }

    @Serializable
    @SerialName("text")
    data class Text(val field: String, val text: String, val match: TextMatch = TextMatch.FULLTEXT) : EarlyFilter {
        init {
            require(field.matches(Regex("[A-Za-z][A-Za-z0-9_]*"))) { "Invalid text filter field." }
            require(text.isNotBlank()) { "Text filters require non-blank text." }
        }
    }

    @Serializable
    @SerialName("exists")
    data class Exists(val field: String) : EarlyFilter {
        init { require(field.matches(Regex("[A-Za-z][A-Za-z0-9_]*"))) { "Invalid existence filter field." } }
    }

    /** Bound SQL-tree size, including trees built programmatically. */
    fun validate() {
        var count = 0
        fun visit(node: EarlyFilter, depth: Int) {
            require(++count <= 256 && depth <= 32) { "Early filters support at most 256 nodes and 32 levels." }
            when (node) {
                is All -> node.conditions.forEach { visit(it, depth + 1) }
                is Any -> node.conditions.forEach { visit(it, depth + 1) }
                is Not -> visit(node.condition, depth + 1)
                else -> Unit
            }
        }
        visit(this, 1)
    }
}
