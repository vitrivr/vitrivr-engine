package org.vitrivr.engine.query.transform.lookup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.DescriptorAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RelationshipAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer

/**
 * Appends [DescriptorAttribute] to a [Retrieved] in a specified object [Relationship] based on lookup values of a [Schema.Field], if available.
 *
 * @version 1.0.0
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class ObjectFieldLookup(override val input: Operator<Retrieved>, private val reader: DescriptorReader<*>, private val predicates: Set<String>) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = input.toFlow(scope).toList()

        /* Fetch Retrievable entries that should be enriched. */
        val enrich = inputRetrieved.mapNotNull { it.filteredAttribute(RelationshipAttribute::class.java) }.flatMap { r ->
            r.relationships.filter { this@ObjectFieldLookup.predicates.isEmpty() || it.pred in this@ObjectFieldLookup.predicates }.mapNotNull { it.obj.second }
        }
        val ids = enrich.map { it.id }
        val descriptors = if (ids.isNotEmpty()) {
            this@ObjectFieldLookup.reader.getAllBy(ids, "retrievableId").filter { it.retrievableId != null }.associateBy { it.retrievableId!! }
        } else {
            emptyMap()
        }

        /* Emit retrievable with added attribute. */
        enrich.forEach {
            val descriptor = descriptors[it.id]
            if (descriptor != null) {
                it.addAttribute(DescriptorAttribute(descriptor))
            }
        }

        /* Emit input. */
        inputRetrieved.forEach { emit(it) }
    }
}