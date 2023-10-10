package org.vitrivr.engine.core.config.pipeline

import kotlinx.serialization.Serializable

@Serializable
data class ExtractorConfig(
    val factory: String,
    val parameters: Map<String, String> = emptyMap(),
    val extractors: List<ExtractorConfig> = emptyList(),
    val exporters: List<ExporterConfig> = emptyList()
)