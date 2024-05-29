package org.vitrivr.engine.core.context

import io.javalin.openapi.OpenApiIgnore
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.vitrivr.engine.core.model.metamodel.Schema


@Serializable
class QueryContext(
    override val local: Map<String, Map<String, String>> = emptyMap(),
    override val global: Map<String, String> = emptyMap()
) : Context() {

    @Transient
    @get:OpenApiIgnore
    override lateinit var schema: Schema
}
