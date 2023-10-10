package org.vitrivr.engine.core.config.pipeline

import kotlinx.serialization.Serializable

@Serializable
data class TransformerConfig(val factory: String, val parameters: Map<String,String> = emptyMap(), val segmenters: List<SegmenterConfig>)