package org.vitrivr.engine.query.operators.transform.relation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer

/**
 * Resolves a [Relationship] of the processed [Retrieved] by the specified relationship predicate,
 * resulting in all, the processed and all related [Retrieved]s to be emitted to the [Flow].
 */
class RelationResolver(
    override val input: Operator<out Retrievable>,
    private val relationPredicate: String,
    private val retrievableReader: RetrievableReader
): Transformer {
    /**
     * Converts this [Operator] to a [Flow] of type [O].
     *
     * @param scope The [CoroutineScope] to execute the [Flow] in.
     * @return Type [O]
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        val inputs = input.toFlow(scope).toList()
        val existingIds = inputs.map { it.id }.toSet()

        /* Fetch ids to resolve */
        val newIds = inputs.flatMap {
            val relationships = it.relationships.filter { r -> r.predicate == relationPredicate }
            relationships.map{r ->
                /* Resolve which one is the new id */
                return@map if(r.subjectId == it.id){
                    r.objectId
                }else{
                    r.subjectId
                }
            }
        }

        /* Deduplicate retrievables which we might already have in the list*/
        val idsToFetch = newIds.filter { it !in existingIds }

        val retrievables = if(idsToFetch.isNotEmpty()){
            retrievableReader.getAll(idsToFetch).toList()
        }else{
            emptyList()
        }

        /* Emit */
        (inputs + retrievables).forEach {
            emit(it)
        }

    }

}
