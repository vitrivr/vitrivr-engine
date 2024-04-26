package org.vitrivr.engine.core.context

import kotlinx.serialization.Serializable

@Serializable
class QueryContext(
    override val local: Map<String, Map<String, String>> = emptyMap(),
    override val global: Map<String, String> = emptyMap()
): Context() {
}
