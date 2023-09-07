package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable

typealias RetrievableIdString = String

@Serializable
data class QueryResultRetrievable(val id: RetrievableIdString, val score: Float, val parts: List<RetrievableIdString>) {
    constructor(scoredRetrievable: ScoredRetrievable) : this(scoredRetrievable.id.toString(), scoredRetrievable.score, scoredRetrievable.parts.map { it.id.toString() })
}
