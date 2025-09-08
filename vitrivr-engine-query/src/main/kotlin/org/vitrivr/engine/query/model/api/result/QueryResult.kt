package org.vitrivr.engine.query.model.api.result

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable

@Serializable
data class QueryResult(val retrievables: List<QueryResultRetrievable>) {

    companion object {

        private fun fromRetrievables(retrieved: Collection<Retrievable>) : List<QueryResultRetrievable> {

            val results = retrieved.map { QueryResultRetrievable(it) }.associateBy { it.id }

            //map partOf relations the right way around
            retrieved.forEach { r: Retrievable ->
                val relationships = r.relationships
                if (relationships.isNotEmpty()) {
                    // Adds object to relationship of subject e.g adds the VIDEO:SOURCE to the SEGMENT. Constrained to depth 1.
                    relationships.filterIsInstance<Relationship.ByRef>().filter { it.predicate == "partOf" && it.subjectId == r.id }.forEach { rel ->
                        results[rel.subjectId.toString()]?.relationship["partOf"] = QueryResultRetrievable(rel.`object`)
                    }
                    // Adds subject to relationship of object e.g adds the SEGMENT to the VIDEO:SOURCE.  Constrained to depth 1.
                    relationships.filterIsInstance<Relationship.ByRef>().filter  { it.predicate == "partOf" && it.objectId == r.id }.forEach {rel ->
                        results[rel.subjectId.toString()]?.relationship["partOf"] = QueryResultRetrievable(rel.subject)
                    }
                }
            }


            return results.values.toList().sortedByDescending { it.score }

        }

    }

    constructor(retrieved: Collection<Retrievable>) : this(fromRetrievables(retrieved))
}
