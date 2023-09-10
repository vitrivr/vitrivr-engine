package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.database.retrievable.Retrieved

@Serializable
data class QueryResult(val retrievables: List<QueryResultRetrievable>) {
    constructor(retrieved: Collection<Retrieved>) : this(retrieved.map { QueryResultRetrievable(it) })
}
