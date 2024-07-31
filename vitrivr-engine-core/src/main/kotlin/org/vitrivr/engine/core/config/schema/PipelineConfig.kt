package org.vitrivr.engine.core.config.schema

import kotlinx.serialization.Serializable

@Serializable
data class PipelineConfig(
    val path: String,
)
