package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable

@Serializable
class ExporterConfig (
    val name: String,
    val exporterFactory: String,
    val resolverFactory: String,
    val exporterParameters: Map<String, String> = emptyMap(),
    val resolverParameters: Map<String, String> = emptyMap()
)
