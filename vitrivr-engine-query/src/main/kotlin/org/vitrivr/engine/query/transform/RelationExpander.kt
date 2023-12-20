package org.vitrivr.engine.query.transform

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

private val logger: KLogger = KotlinLogging.logger {}

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

        val newRetrievables = if (newIds.isNotEmpty()) retrievableReader.getAll(newIds.toList()) else emptySequence()

        val allRetrieved =
            (inputRetrieved.map { Retrieved.PlusRelationship(it) } + newRetrievables.map { Retrieved.WithRelationship(it) }).associateBy { it.id }

        // TODO: error on VBSLHE dataset
        // TODO: Undefined relationship (e8431942-6fb4-45de-8a35-a417e89c68fe, partOf, 51e5b405-2fe7-4f30-a39a-c1892e9a0372)
        for (relation in relations) {
            val sub = allRetrieved[relation.first]
            val obj = allRetrieved[relation.third]
            if (sub == null || obj ==null) {
                logger.error { "Undefined relationship ${relation}" }
                continue
            }


            val expandedRelation = Relationship(sub, relation.second, obj)

            (sub.relationships as MutableSet).add(expandedRelation)
            (obj.relationships as MutableSet).add(expandedRelation)
        }


        allRetrieved.values.forEach {
            emit(it)
        }
    }
}