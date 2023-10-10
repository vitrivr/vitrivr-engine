package org.vitrivr.engine.core.config.pipeline

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.ResolverConfig

@Serializable
data class ExporterConfig(
    val factory: String,
    val resolver: ResolverConfig,
    val parameters: Map<String, String> = emptyMap(),
    val extractors: List<ExtractorConfig> = emptyList(),
    val exporters: List<ExporterConfig> = emptyList(),
)