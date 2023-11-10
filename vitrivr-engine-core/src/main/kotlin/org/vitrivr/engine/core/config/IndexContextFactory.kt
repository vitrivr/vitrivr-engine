package org.vitrivr.engine.core.config

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.ResolverFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName

/**
 *
 */
object IndexContextFactory {
    fun newContext(schema: Schema, contextConfig: ContextConfig): IndexContext {
        /* Load content factory. */
        val contentFactory = loadServiceForName<ContentFactory>(contextConfig.contentFactory) ?: throw IllegalArgumentException("Failed to find content factory implementation for name '${contextConfig.contentFactory}'.")

        /* Load default resolver. */
        val resolverFactory = loadServiceForName<ResolverFactory>(contextConfig.resolverFactory) ?: throw IllegalArgumentException("Failed to find resolver implementation for name '${contextConfig.resolverFactory}'.")

        /* Return new context. */
        return IndexContext(schema, contentFactory, resolverFactory.newResolver(contextConfig.parameters))
    }
}