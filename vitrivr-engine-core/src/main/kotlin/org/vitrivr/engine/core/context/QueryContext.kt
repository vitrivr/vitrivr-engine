package org.vitrivr.engine.core.context

import io.javalin.openapi.OpenApiIgnore
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * [Context] used for queries.
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
@Serializable
class QueryContext(
    override val local: Map<String, Map<String, String>> = emptyMap(),
    override val global: Map<String, String> = emptyMap()
) : Context() {

    /**
     *
     */
    companion object {
        /** The key for the [QueryContext] limit parameter. */
        const val LIMIT_KEY = "limit"

        /** The key for the [QueryContext] 'fetch_descriptor' parameter. */
        const val FETCH_DESCRIPTOR_KEY = "returnDescriptor"

        /** The default for the [QueryContext] limit parameter. */
        const val LIMIT_DEFAULT = 1000L
    }

    @Transient
    @get:OpenApiIgnore
    override lateinit var schema: Schema


}
