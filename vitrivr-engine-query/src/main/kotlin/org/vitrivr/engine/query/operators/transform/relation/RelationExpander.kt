package org.vitrivr.engine.query.operators.transform.relation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

/**
 * Appends [Relationship] to a [Retrieved] by expanding the specified incoming and outgoing relationships.
 *
 * @version 1.2.0
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class RelationExpander(
    override val input: Operator<Retrieved>,
    private val incomingRelations: List<String>,
    private val outgoingRelations: List<String>,
    private val retrievableReader: RetrievableReader
) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        /* Collect input into list. */
        val inputRetrieved = input.toFlow(scope).toList()

        /* Fetch relation entries for the provided IDs. */
        val ids = inputRetrieved.map { it.id }.toSet()
        val (objects, subjects) = if (ids.isNotEmpty()) {
            (if (this@RelationExpander.incomingRelations.isNotEmpty()) {
                this@RelationExpander.retrievableReader.getConnections(emptyList(), this@RelationExpander.incomingRelations, ids)
            } else {
                emptySequence()
            }.groupBy { it.third }

            to

            if (this@RelationExpander.outgoingRelations.isNotEmpty()) {
                this@RelationExpander.retrievableReader.getConnections(ids, this@RelationExpander.outgoingRelations, emptyList())
            } else {
                emptySequence()
            }.groupBy { it.first })
        } else {
            emptyMap<RetrievableId, List<Triple<RetrievableId,String,RetrievableId>>>() to emptyMap()
        }

        /* Collection IDs that are new and fetch corresponding retrievable. */
        val newIds = (objects.values.flatMap { o -> o.map { s -> s.first } }.toSet() + subjects.values.flatMap { s -> s.map { o -> o.third } }.toSet()) - ids
        val new = if (newIds.isNotEmpty()) {
            this@RelationExpander.retrievableReader.getAll(newIds.toList())
        } else {
            emptySequence()
        }.map {
            Retrieved(it)
        }.associateBy {
            it.id
        }

        /* Iterate over input and emit each retrievable with expanded relationships. */
        inputRetrieved.forEach {
            /* Expand incoming relationships. */
            for (obj in (objects[it.id] ?: emptyList())) {
                val subject = new[obj.third]
                if (subject != null) {
                    it.addRelationship(Relationship.ByRef(it, obj.second, subject))
                }
            }

            /* Expand outgoing relationships. */
            for (sub in (subjects[it.id] ?: emptyList())) {
                val `object` = new[sub.third]
                if (`object` != null) {
                    it.addRelationship(Relationship.ByRef(it, sub.second, `object`))

                }
            }

            /* Emit. */
            emit(it)
        }
    }
}