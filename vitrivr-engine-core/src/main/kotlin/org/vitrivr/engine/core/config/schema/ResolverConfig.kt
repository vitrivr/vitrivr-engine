package org.vitrivr.engine.core.config.schema

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.resolver.Resolver
import org.vitrivr.engine.core.resolver.ResolverFactory

/**
 * The [ResolverConfig] configures the instantiation of a [Resolver].
 *
 * @author Fynn Faber
 * @version 1.0.0.
 */
@Serializable
data class ResolverConfig(
    /**
     * The simple or fully qualified name of the [ResolverFactory] that will create the [Resolver] configured by this [ResolverConfig].
     */
    val factory: String,
    /**
     * A map of key-value pairs that additionally configure the to-be-created [Resolver].
     * Keys are defined on the [Resolver] or [ResolverFactory].
     */
    val parameters: Map<String, String> = emptyMap()
)
