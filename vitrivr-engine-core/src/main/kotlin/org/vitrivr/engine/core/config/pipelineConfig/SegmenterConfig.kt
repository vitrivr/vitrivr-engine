package org.vitrivr.engine.core.config.pipelineConfig

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.ExporterConfig

@Serializable
data class SegmenterConfig(
    val name: String,
    val type: String,
    val parameters: Map<String, String> = emptyMap(),
    val extractors: List<ExtractorConfig> = emptyList(),
    val exporters: List<ExporterConfig> = emptyList(),
)