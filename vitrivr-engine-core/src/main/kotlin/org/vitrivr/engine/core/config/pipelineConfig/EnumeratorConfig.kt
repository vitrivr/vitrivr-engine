package org.vitrivr.engine.core.config.pipelineConfig

import kotlinx.serialization.Serializable

@Serializable
data class EnumeratorConfig(val name: String, val type: String, val parameters: Map<String, String> = emptyMap(), val decoder: DecoderConfig)