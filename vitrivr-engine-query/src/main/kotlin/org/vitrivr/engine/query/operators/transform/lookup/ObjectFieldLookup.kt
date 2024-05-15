package org.vitrivr.engine.query.operators.transform.lookup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer

/**
 * Appends [DescriptorAttribute] to a [Retrieved] in a specified object [Relationship] based on lookup values of a [Schema.Field], if available.
 *
 * @version 1.0.1
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class ObjectFieldLookup(override val input: Operator<out Retrievable>, private val reader: DescriptorReader<*>, private val predicates: Set<String>) :
    Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = this@ObjectFieldLookup.input.toFlow(scope).toList()

        /* Determine retrievables that should be enriched. */
        val enrich = inputRetrieved.flatMap { retrieved ->
            retrieved.relationships.filterIsInstance<Relationship.ByRef>().filter { this@ObjectFieldLookup.predicates.isEmpty() || it.predicate in this@ObjectFieldLookup.predicates }.map {
                if (retrieved == it.`object`) {
                    it.subject.id to it.subject
                } else {
                    it.`object`.id to it.`object`
                }
            }
        }.toMap()

        /* Fetch descriptors for retrievables that should be enriched. */
        val descriptors = if (enrich.isNotEmpty()) {
            this@ObjectFieldLookup.reader.getAllBy(enrich.keys, "retrievableId").filter { it.retrievableId != null }.associateBy { it.retrievableId!! }
        } else {
            emptyMap()
        }

        /* Emit retrievable with added attribute. */
        enrich.forEach { (k, v) ->
            val descriptor = descriptors[k]
            if (descriptor != null) {
                v.addDescriptor(descriptor)
            }
        }

        /* Emit input. */
        inputRetrieved.forEach { emit(it) }
    }
}