package org.vitrivr.engine.core.context

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

    /** The name of the [Resolver] to be used during ingestion */
    val resolverName: String,

    /** [Map] of local parameters (applied to one operator). */
    override val local: Map<String, Map<String, String>> = emptyMap(),

    /** [Map] of global parameters. */
    override val global: Map<String, String> = emptyMap()
) : Context() {

    @Transient
    override lateinit var schema: Schema

}