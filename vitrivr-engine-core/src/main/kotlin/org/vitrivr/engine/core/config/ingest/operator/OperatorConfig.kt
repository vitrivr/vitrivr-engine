package org.vitrivr.engine.core.config.ingest.operator


import kotlinx.serialization.Serializable

/**
 * Configuration for ingestion operators, as defined in the [org.vitrivr.engine.core.operators.ingest] package.
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
@Serializable
data class OperatorConfig(
    val field: String? = null,
    val factory: String? = null,
    val exporter: String? = null,
    val parameters: Map<String, String> = emptyMap()
) {
    init {
        require(this.field != null || this.factory != null || this.exporter != null) {
            "An OperatorConfig must have either an field name, exporter name or factory name."
        }
    }
}