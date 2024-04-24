package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable


@Deprecated("Since the introduction of the IngestionContextConfig")
@Serializable
data class ContextConfig(val contentFactory: String, val resolverFactory: String, val parameters: Map<String, String> = emptyMap())
