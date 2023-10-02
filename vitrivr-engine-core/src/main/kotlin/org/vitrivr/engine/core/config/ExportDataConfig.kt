package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable

@Serializable
class ExportDataConfig (
    val name: String,
    val exporter: String,
    val parameters: Map<String, String> = emptyMap())
