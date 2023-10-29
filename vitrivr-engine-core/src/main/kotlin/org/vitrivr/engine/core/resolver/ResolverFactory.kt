package org.vitrivr.engine.core.resolver

/**
 * A factory class for [Resolver]s.
 *
 * @author Finn Faber
 * @version 1.0.0.
 */
interface ResolverFactory {

    /**
     * Generates a new [Resolver] instance using the provided [parameters].
     *
     * @param parameters The parameters used to configure [Resolver]
     * @return [Resolver]
     */
    fun newResolver(parameters: Map<String, Any>): Resolver
}