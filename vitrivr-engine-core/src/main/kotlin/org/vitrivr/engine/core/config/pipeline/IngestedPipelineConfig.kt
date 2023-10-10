package org.vitrivr.engine.core.config.pipeline

interface IngestedPipelineConfig {
    val name: String
    val parameters: Map<String, String>
    val childs: List<IngestedPipelineConfig>
}