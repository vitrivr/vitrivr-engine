package org.vitrivr.engine.index.config.pipeline

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * Configuration element for an [Extractor] stage in an operator pipeline.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
@Serializable
data class ExtractorConfig(
    val fieldName: String? = null,
    val factoryName: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val nextExtractor: ExtractorConfig? = null,
    val nextExporter: ExporterConfig? = null
) {
    init {
        require(this.fieldName != null || this.factoryName != null) { "An extractor must have either a field name or a factory name." }
        if (this.nextExtractor != null) {
            require(this.nextExporter == null) { "An extractor can either be followed by another extractor OR an exporter." }
        }
    }
}