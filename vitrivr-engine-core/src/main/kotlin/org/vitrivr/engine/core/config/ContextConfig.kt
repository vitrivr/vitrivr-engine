package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
data class ContextConfig(val contentFactory: ContentFactoryConfig, val resolver: ResolverConfig)