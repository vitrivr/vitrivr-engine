package org.vitrivr.engine.core.config

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.model.content.factory.ContentFactoriesFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.util.extension.loadServiceForName

/**
 * The [IndexContextFactory] creates the appropriate [IndexContext], based on the [Schema] and [IngestionContextConfig].
 */
object IndexContextFactory {

    /**
     * Primarily creates the [ContentFactoriesFactory], on which foundation the [IndexContext] is created.
     *
     * @param contextConfig The [IngestionContextConfig] describing the [IndexContext] to-be-built.
     *
     * @return A [IndexContext] based on the [contextConfig]'s description and for the [schema].
     */
    fun newContext(contextConfig: IngestionContextConfig): IndexContext {
        /* Load content factory. */
        val contentFactory = loadServiceForName<ContentFactoriesFactory>(contextConfig.contentFactory) ?: throw IllegalArgumentException("Failed to find content factory implementation for name '${contextConfig.contentFactory}'.")

        val schema = contextConfig.schema

        /* Return new context. */
        return IndexContext(
            schema,
            contentFactory.newContentFactory(schema, contextConfig),
            contextConfig.resolvers.associateWith { k -> schema.getResolver(k) },
            contextConfig.local,
            contextConfig.global
        )
    }
}
