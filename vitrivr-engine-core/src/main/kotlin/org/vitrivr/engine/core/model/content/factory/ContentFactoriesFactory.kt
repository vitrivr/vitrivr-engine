package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.Resolver

/**
 * A factory class for [Resolver]s.
 *
 * @version 1.0.0.
 */
interface ContentFactoriesFactory {

    /**
     * Generates a new [Resolver] instance using the provided [parameters].
     *
     * @param schema The [Schema] on which the [Resolver] operates
     * @param parameters The parameters used to configure [Resolver]
     * @return [Resolver]
     */
    fun newContentFactory(schema: Schema, parameters: Map<String, String>): ContentFactory
}