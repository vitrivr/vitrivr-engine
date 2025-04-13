package org.vitrivr.engine.core.operators.transform.shape

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.NAry] that merges multiple [Operator]s into a single [Flow].
 *
 * <strong>Attention:</strong> The output order of the emitted elements is not defined!
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class MergeOperator(override val inputs: List<Operator<Retrievable>>, override val name: String = "merge") : Operator.NAry<Retrievable, Retrievable> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
        val jobs = this@MergeOperator.inputs.map { p ->
            launch {
                p.toFlow(scope).collect { send(it) }
            }
        }
        jobs.forEach { it.join() }
    }
}