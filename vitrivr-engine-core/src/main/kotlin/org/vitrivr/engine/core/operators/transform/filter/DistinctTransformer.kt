package org.vitrivr.engine.core.operators.transform.filter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import java.util.*

/**
 * A [Transformer] that makes sure that every [Retrievable] object is only emitted once.
 *
 * Comparison is based on the [Retrievable.id] of the [Retrievable] objects.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class DistinctTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<Retrievable>, context: IndexContext): Transformer = Instance(input)

    /**
     * [Transformer] that extracts [Retrievable] objects from a [Flow] of [Retrievable] objects based on a given [Relationship].
     */
    private class Instance(override val input: Operator<Retrievable>) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
            val set = HashSet<UUID>()
            this@Instance.input.toFlow(scope).collect {
                if (it.id !in set) {
                    set.add(it.id)
                    send(it)
                }
            }
        }
    }
}