package org.vitrivr.engine.core.config.operators

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.operators.ExporterConfig
import org.vitrivr.engine.core.config.operators.ExtractorConfig


/**
 * Configuration element for a [ContentAggregator] stage in an operator pipeline.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Deprecated(
    message="Replaced by the new extraction pipeline definition language in package package org.vitrivr.engine.core.config.ingest",
    replaceWith = ReplaceWith("org.vitrivr.engine.core.config.ingest.operator.AggregatorConfig"),
    level = DeprecationLevel.WARNING
)
@Serializable
data class AggregatorConfig(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
    val nextExtractor: ExtractorConfig? = null,
    val nextExporter: ExporterConfig? = null
) {
    init {
        if (this.nextExtractor != null) {
            require(this.nextExporter == null) { "An aggregator can either be followed by an extractor OR an exporter." }
        } else {
            require(this.nextExporter != null) { "An aggregator can either be followed by an extractor OR an extractor." }
        }
    }
}
