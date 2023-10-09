package org.vitrivr.engine.core.config.pipelineConfig

interface IngestedPipelineConfig {
    val name: String
    val parameters: Map<String, String>
    val childs: List<IngestedPipelineConfig>
}