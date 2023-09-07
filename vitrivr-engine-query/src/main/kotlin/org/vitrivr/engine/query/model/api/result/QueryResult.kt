package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable

@Serializable
data class QueryResult(val retrievables: List<QueryResultRetrievable>) {
    constructor(retrievables: Collection<ScoredRetrievable>) : this (retrievables.map { QueryResultRetrievable(it) })
}
