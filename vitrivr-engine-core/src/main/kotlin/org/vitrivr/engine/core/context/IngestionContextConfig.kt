package org.vitrivr.engine.core.context

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.resolver.Resolver

/**
 * The [IngestionContextConfig] describes the [IndexContext] to be used during ingestion.
 */
@Serializable
class IngestionContextConfig(
    /** The simple or fully qualified class name of the [ContentFactory] to be used to construct the [IndexContext] */
    val contentFactory: String,
    /** The name of the [Resolver] to be used during ingestion */
    val resolverName: String,
    override val local: Map<String, Map<String, String>> = emptyMap(),
    override val global: Map<String, String> = emptyMap()
) : Context()