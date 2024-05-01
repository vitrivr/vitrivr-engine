package org.vitrivr.engine.core.operators.transform.map

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * A [Transformer] that resolves a specified relationship for all incoming [Ingested]and emits the [Ingested] reference on the other side of the relationship.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class MapRelationshipTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<Retrievable>, context: Context): Transformer {
        val predicate = context[name, "predicate"] ?: throw IllegalArgumentException("The relationship transformer requires a predicate to be specified.")
        return Instance(input, predicate)
    }

    /**
     * [Transformer] that extracts [Ingested] objects from a [Flow] of [Ingested] objects based on a given [Relationship].
     */
    private class Instance(override val input: Operator<Retrievable>, val name: String) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Ingested> = channelFlow {
            this@Instance.input.toFlow(scope).collect { ingested ->
                ingested.relationships.filter { it.predicate == this@Instance.name }.forEach { relationship ->
                    if (relationship.subjectId == ingested.id && relationship is Relationship.WithObject) {
                        send(relationship.`object` as Ingested)
                    } else if (relationship.objectId == ingested.id && relationship is Relationship.WithSubject) {
                        send(relationship.subject as Ingested)
                    }
                }
            }
        }
    }
}