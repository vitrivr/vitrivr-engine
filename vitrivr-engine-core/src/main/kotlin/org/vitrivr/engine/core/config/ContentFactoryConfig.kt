package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable

@Serializable
data class ContentFactoryConfig(
    val factory: String,
    val parameters: Map<String, String> = emptyMap()
)
