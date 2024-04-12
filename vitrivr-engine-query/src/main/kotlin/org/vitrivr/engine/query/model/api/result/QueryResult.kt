package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.retrievable.Retrieved

@Serializable
data class QueryResult(val retrievables: List<QueryResultRetrievable>) {

    companion object {

        private fun fromRetrieved(retrieved: Collection<Retrieved>) : List<QueryResultRetrievable> {

            val results = retrieved.map { QueryResultRetrievable(it) }.associateBy { it.id }

            //map partOf relations the right way around
            retrieved.forEach { r: Retrieved ->
                val relationships = r.relationships
                if (relationships.isNotEmpty()) {
                    relationships.filter { it.predicate == "partOf" && it.subjectId == r.id }.forEach {
                        results[it.objectId.toString()]?.parts?.add(r.id.toString())
                    }
                    relationships.filter { it.predicate == "partOf" && it.objectId == r.id }.forEach {
                        results[r.id.toString()]?.parts?.add(it.subjectId.toString())
                    }
                }
            }

            return results.values.toList().sortedByDescending { it.score }

        }

    }

    constructor(retrieved: Collection<Retrieved>) : this(fromRetrieved(retrieved))
}
