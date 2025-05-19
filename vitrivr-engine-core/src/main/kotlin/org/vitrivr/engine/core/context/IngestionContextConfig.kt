package org.vitrivr.engine.core.context

import io.javalin.openapi.OpenApiIgnore
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.Resolver

/**
 * The [IngestionContextConfig] describes the [IndexContext] to be used during ingestion.
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
@Serializable
class IngestionContextConfig(

    /** The simple or fully qualified class name of the [ContentFactory] to be used to construct the [IndexContext] */
    val contentFactory: String,

    /** The name of the [Resolver]s to be used during ingestion */
    val resolvers: List<String>,

    ) {
    @Transient
    @get:OpenApiIgnore
    @set:OpenApiIgnore
    lateinit var schema: Schema
}
