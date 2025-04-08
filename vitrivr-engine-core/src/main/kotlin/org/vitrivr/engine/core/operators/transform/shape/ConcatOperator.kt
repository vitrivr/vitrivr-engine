package org.vitrivr.engine.core.operators.transform.shape

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * [CombineOperator] merges the results of multiple incoming [Operator]s into a single [Flow] by collecting them in order.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ConcatOperator(override val inputs: List<Operator<Retrievable>>, override val name: String = "concat") : Operator.NAry<Retrievable, Retrievable> {

    /**
     *  Generates a new [channelFlow] that merges the results of multiple incoming [Operator]s into a single [Flow]. Incoming operators are
     *  executed in a new [CoroutineScope] and the order of the [Retrievable]s in the outgoing [Flow] may be arbitrary.
     *
     *  @param scope The [CoroutineScope] of the calling [Operator]
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
        val flows = this@ConcatOperator.inputs.map { p -> p.toFlow(this) }
        flows.forEach { f ->
            f.collect {
                send(it)
            }
        }
    }
}