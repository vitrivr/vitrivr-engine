package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.retrievable.Retrieved

typealias RetrievableIdString = String

@Serializable
data class QueryResultRetrievable(val id: RetrievableIdString, val score: Float, val type: String, val parts: MutableList<RetrievableIdString>) {
    constructor(retrieved: Retrieved) : this(
        retrieved.id.toString(),
        if (retrieved is Retrieved.RetrievedWithScore) retrieved.score else 0f,
        retrieved.type ?: "",
        mutableListOf()
    )
}
