package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.pipelineConfig.ExtractorConfig
import org.vitrivr.engine.core.config.pipelineConfig.IngestedPipelineConfig
import org.vitrivr.engine.core.model.database.retrievable.Ingested

@Serializable
data class ExporterConfig(
    val name: String,
    val factory: String = "",
    val resolver: ResolverConfig,
    val parameters: Map<String, String> = emptyMap(),
    val extractors: List<ExtractorConfig> = emptyList(),
    val exporters: List<ExporterConfig> = emptyList(),
)