package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.Resolver
import org.vitrivr.engine.core.context.Context

/**
 * A factory class for [Resolver]s.
 *
 * @version 1.0.0.
 */
interface ContentFactoriesFactory {

    /**
     * Generates a new [Resolver] instance using the provided [context].
     *
     * @param schema The [Schema] on which the [Resolver] operates
     * @param context The currently active [Context]
     * @return [Resolver]
     */
    fun newContentFactory(schema: Schema, context: Context): ContentFactory
}
