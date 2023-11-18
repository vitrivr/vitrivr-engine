package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.retrievable.Retrieved

@Serializable
data class QueryResult(val retrievables: List<QueryResultRetrievable>) {

    companion object {

        private fun fromRetrieved(retrieved: Collection<Retrieved>) : List<QueryResultRetrievable> {

            val results = retrieved.map { QueryResultRetrievable(it) }.associateBy { it.id }

            //map partOf relations the right way around
            retrieved.forEach { retrieved: Retrieved ->
                if (retrieved is Retrieved.RetrievedWithRelationship) {
                    retrieved.relationships["partOf"]?.forEach {
                        results[it.id.toString()]?.parts?.add(retrieved.id.toString())
                    }
                }
            }

            return results.values.toList().sortedBy { it.score }

        }

    }

    constructor(retrieved: Collection<Retrieved>) : this(fromRetrieved(retrieved))
}
