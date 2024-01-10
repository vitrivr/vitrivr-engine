package org.vitrivr.engine.core.resolver

import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A factory class for [Resolver]s.
 *
 * @version 1.0.0.
 */
interface ResolverFactory {

    /**
     * Generates a new [Resolver] instance using the provided [parameters].
     *
     * @param schema The [Schema] on which the [Resolver] operates
     * @param parameters The parameters used to configure [Resolver]
     * @return [Resolver]
     */
    fun newResolver(schema: Schema, parameters: Map<String, String>): Resolver
}