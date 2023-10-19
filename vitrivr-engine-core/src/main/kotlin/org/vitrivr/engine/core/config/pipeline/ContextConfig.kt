package org.vitrivr.engine.core.config.pipeline

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.content.ContentFactory

@Serializable
data class ContextConfig (val contentFactory: String, val parameters: Map<String,String> = emptyMap())