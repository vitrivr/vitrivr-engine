package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable

@Serializable
data class ExporterConfig(
    val name: String,
    val factory: String,
    val resolver: ResolverConfig,
    val parameters: Map<String, String> = emptyMap(),
)