package org.vitrivr.engine.core.config.operators

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Exporter

/**
 * Configuration element for an [Exporter] stage in an operator pipeline.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
@Serializable
@Deprecated(
    message = "Replaced by the new extraction pipeline definition language in package package org.vitrivr.engine.core.config.ingest",
    replaceWith = ReplaceWith("org.vitrivr.engine.core.config.ingest.operator.ExporterConfig"),
    level = DeprecationLevel.WARNING
)
data class ExporterConfig(
    val name: String,
    val exporterName: String? = null,
    val factoryName: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val nextExtractor: ExtractorConfig? = null,
    val nextExporter: ExporterConfig? = null
) {
    init {
        require(this.exporterName != null || this.factoryName != null) { "An extractor must have either a field name or a factory name." }
        if (this.nextExtractor != null) {
            require(this.nextExporter == null) { "An exporter can either be followed by another exporter OR an extractor." }
        }
    }
}
