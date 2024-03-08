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
import org.vitrivr.engine.core.model.retrievable.attributes.RelationshipAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

class RelationExpander(
    override val input: Operator<Retrieved>,
    private val incomingRelations: List<String>,
    private val outgoingRelations: List<String>,
    private val retrievableReader: RetrievableReader
) : Transformer {

    private val logger: KLogger = KotlinLogging.logger {}

    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {

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

        val allRetrieved = (inputRetrieved + newRetrievables.map { Retrieved(it) }).associateBy { it.id }


        for (relation in relations) {
            val sub = allRetrieved[relation.first]
            val obj = allRetrieved[relation.third]
            if (sub == null || obj == null) {
                logger.error { "Undefined relationship ${relation}" }
                continue
            }


            val expandedRelation = Relationship(sub, relation.second, obj)
            val attribute = RelationshipAttribute(expandedRelation)

            sub.addAttribute(attribute)
            obj.addAttribute(attribute)
        }


        allRetrieved.values.forEach {
            emit(it)
        }
    }
}