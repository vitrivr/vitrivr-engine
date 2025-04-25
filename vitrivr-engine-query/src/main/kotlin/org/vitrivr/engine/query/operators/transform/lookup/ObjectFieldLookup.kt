package org.vitrivr.engine.query.operators.transform.lookup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer

/**
 * Appends [Descriptor]s to a [Retrieved] in a specified object [Relationship] based on lookup values of a [Schema.Field], if available.
 *
 * @version 1.2.0
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
class ObjectFieldLookup(override val input: Operator<out Retrievable>,
                        private val reader: DescriptorReader<*>,
                        private val predicates: Set<String>,
                        override val name: String
) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = flow {
        /* Parse input IDs.*/
        val inputRetrieved = this@ObjectFieldLookup.input.toFlow(scope).toList()

        /* Determine retrievables that should be enriched. */
        val enrich = inputRetrieved.flatMap { retrieved ->
            retrieved.relationships.filterIsInstance<Relationship.ByRef>().filter { this@ObjectFieldLookup.predicates.isEmpty() || it.predicate in this@ObjectFieldLookup.predicates }.map {
                if (retrieved == it.`object`) {
                    it.subjectId
                } else {
                    it.objectId
                }
            }
        }.toSet()

        /* Fetch descriptors for retrievables that should be enriched. */
        val descriptors = if (enrich.isNotEmpty()) {
            this@ObjectFieldLookup.reader.getAllForRetrievable(enrich).associateBy { it.retrievableId!! }
        } else {
            emptyMap()
        }

        /* Emit input. */
        inputRetrieved.forEach { it ->
            val relationships = it.relationships.toMutableSet() /* This is a hack to work around the immutability of Retrievables. */
            val new = mutableSetOf<Relationship.ByRef>()
            val retrievable = it.copy(relationships = relationships)
            relationships.removeIf { rel ->
                if (rel is Relationship.ByRef && (this@ObjectFieldLookup.predicates.isEmpty() || rel.predicate in this@ObjectFieldLookup.predicates)) {
                    if (descriptors.containsKey(rel.objectId)) {
                        new.add(Relationship.ByRef(retrievable, rel.predicate, rel.`object`.copy(descriptors = rel.`object`.descriptors + descriptors[rel.objectId]!!), rel.transient))
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

            if (new.isNotEmpty()) {
                relationships.addAll(new)
                emit(retrievable)
            } else {
                emit(it)
            }
        }
    }
}