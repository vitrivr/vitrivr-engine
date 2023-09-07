package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(val retrievables: List<QueryResultRetrievable>)
