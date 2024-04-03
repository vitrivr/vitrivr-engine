package org.vitrivr.engine.query.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Relationship
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.RelationshipAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

/**
 * Appends [RelationshipAttribute] to a [Retrieved] by expanding the specified incoming and outgoing relationships.
 *
 * @version 1.1.0
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
            }.groupBy { it.first }

            to

            if (this@RelationExpander.outgoingRelations.isNotEmpty()) {
                this@RelationExpander.retrievableReader.getConnections(ids, this@RelationExpander.outgoingRelations, emptyList())
            } else {
                emptySequence()
            }.groupBy { it.third })
        } else {
            emptyMap<RetrievableId, List<Triple<RetrievableId,String,RetrievableId>>>() to emptyMap()
        }

        /* Collection IDs that are new and fetch corresponding retrievable. */
        val newIds = (objects.keys + subjects.keys) - ids
        val new = if (newIds.isNotEmpty()) {
            retrievableReader.getAll(newIds.toList())
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
                it.addAttribute(RelationshipAttribute(Relationship(obj.first to it, obj.second, obj.third to new[obj.third])))
            }

            /* Expand outgoing relationships. */
            for (out in (subjects[it.id] ?: emptyList())) {
                it.addAttribute(RelationshipAttribute(Relationship(out.first to new[out.third], out.second, out.first to it)))
            }

            /* Emit. */
            emit(it)
        }
    }
}