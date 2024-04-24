package org.vitrivr.engine.core.config

import org.vitrivr.engine.core.config.ingest.IngestionContextConfig
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.util.extension.loadServiceForName

/**
 *
 */
object IndexContextFactory {
    fun newContext(schema: Schema, contextConfig: IngestionContextConfig): IndexContext {
        /* Load content factory. */
        val contentFactory = loadServiceForName<ContentFactory>(contextConfig.contentFactory) ?: throw IllegalArgumentException("Failed to find content factory implementation for name '${contextConfig.contentFactory}'.")

        /* Return new context. */
        return IndexContext(schema, contentFactory, schema.getResolver(contextConfig.resolverName))
    }
}
