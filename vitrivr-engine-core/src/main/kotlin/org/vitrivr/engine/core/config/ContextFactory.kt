package org.vitrivr.engine.core.config

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.model.content.factory.ContentFactoriesFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.util.extension.loadServiceForName

/**
 * The [ContextFactory] creates the appropriate [Context], based on the [Schema] and [IngestionContextConfig].
 */
object ContextFactory {

    /**
     * Primarily creates the [ContentFactoriesFactory], on which foundation the [Context] is created.
     *
     * @param config The [IngestionContextConfig] describing the [Context] to-be-built.
     *
     * @return A [Context] based on the [config]'s description and for the [schema].
     */
    fun newContext(schema: Schema, config: IngestionContextConfig, parameters: Map<String, String> = emptyMap()): Context {
        val contentFactory = loadServiceForName<ContentFactoriesFactory>(config.contentFactory) ?: throw IllegalArgumentException("Failed to find content factory implementation for name '${config.contentFactory}'.")
        return Context(
            schema,
            contentFactory.newContentFactory(schema, parameters),
            config.resolvers.associateWith { k -> schema.getResolver(k) },
        )
    }
}
