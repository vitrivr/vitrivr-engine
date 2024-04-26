package org.vitrivr.engine.core.config

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.factory.ContentFactoriesFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.ResolverFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName

/**
 *
 */
object IndexContextFactory {
    fun newContext(schema: Schema, contextConfig: ContextConfig): IndexContext {
        /* Load content factory. */
        val contentFactory = loadServiceForName<ContentFactoriesFactory>(contextConfig.contentFactory.factory) ?: throw IllegalArgumentException("Failed to find content factory implementation for name '${contextConfig.contentFactory.factory}'.")

        /* Load default resolver. */
        val resolverFactory = loadServiceForName<ResolverFactory>(contextConfig.resolver.factory) ?: throw IllegalArgumentException("Failed to find resolver implementation for name '${contextConfig.resolver.factory}'.")

        /* Return new context. */
        return IndexContext(schema, contentFactory.newContentFactory(schema, contextConfig.contentFactory.parameters), resolverFactory.newResolver(schema, contextConfig.resolver.parameters))
    }
}