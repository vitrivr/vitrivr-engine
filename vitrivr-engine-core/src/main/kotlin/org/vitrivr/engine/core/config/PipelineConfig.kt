package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable
import java.nio.file.Path
import java.security.cert.CertPath

@Serializable
data class PipelineConfig(
    val name: String,
    val path: String,
)
