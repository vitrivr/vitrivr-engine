package org.vitrivr.engine.core.config

import kotlinx.serialization.Serializable

/**
 * A configuration class for resolvers.
 *
 * @author Fynn Faber
 * @version 1.0.0.
 */
@Serializable
data class ResolverConfig(
    val factory: String,
    val parameters: Map<String, String> = emptyMap()
)