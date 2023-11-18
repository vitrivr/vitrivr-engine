package org.vitrivr.engine.query.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

class RelationExpander(
    override val input: Operator<Retrieved>,
    private val incomingRelations: List<String>,
    private val outgoingRelations: List<String>,
    private val retrievableReader: RetrievableReader
) : Transformer<Retrieved, Retrieved.RetrievedWithRelationship> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved.RetrievedWithRelationship> = flow {

        val inputRetrieved = input.toFlow(scope).toList()

        val ids = inputRetrieved.map { it.id }.toSet()

        if (ids.isEmpty()) {
            return@flow
        }

        val relations = (if (incomingRelations.isNotEmpty()) {
            retrievableReader.getConnections(emptyList(), incomingRelations, ids)
        } else {
            emptySequence()
        } + if (outgoingRelations.isNotEmpty()) {
            retrievableReader.getConnections(ids, outgoingRelations, emptyList())
        } else {
            emptySequence()
        }).toList()

        val newIds = relations.flatMap { listOf(it.first, it.third) } - ids

        val newRetrievables = retrievableReader.getAll(newIds.toList())

        val allRetrieved =
            (inputRetrieved.map { Retrieved.PlusRelationship(it) } + newRetrievables.map { Retrieved.WithRelationship(it) }).associateBy { it.id }

        relations.forEach { relation ->
            val subject = allRetrieved[relation.first]!! //TODO handling missing entries more gracefully?
            val obj = allRetrieved[relation.third]!!
            if (subject.relationships.containsKey(relation.second)) {
                (subject.relationships[relation.second]!! as MutableList).add(obj)
            } else {
                (subject.relationships as MutableMap)[relation.second] = mutableListOf(obj)
            }
        }

        allRetrieved.values.forEach {
            emit(it)
        }
    }
}