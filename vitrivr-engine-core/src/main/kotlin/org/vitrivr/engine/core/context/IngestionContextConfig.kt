package org.vitrivr.engine.core.context

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.resolver.Resolver

/**
 * The [IngestionContextConfig] describes the [Context] to be used during ingestion.
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
@Serializable
class IngestionContextConfig(
    /** The simple or fully qualified class name of the [ContentFactory] to be used to construct the [Context] */
    val contentFactory: String,

    /** The name of the [Resolver]s to be used during ingestion */
    val resolvers: List<String>
)