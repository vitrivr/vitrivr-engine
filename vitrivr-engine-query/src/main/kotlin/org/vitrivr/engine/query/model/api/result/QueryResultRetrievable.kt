package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.database.retrievable.Retrieved

typealias RetrievableIdString = String

@Serializable
data class QueryResultRetrievable(val id: RetrievableIdString, val score: Float, val parts: List<RetrievableIdString>) {
    constructor(scoredRetrievable: Retrieved) : this(scoredRetrievable.id.toString(), 0.0f, emptyList()) /* TODO: Extract parts. */
}
