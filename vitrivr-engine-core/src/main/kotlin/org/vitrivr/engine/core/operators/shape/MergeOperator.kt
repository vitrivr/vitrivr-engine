package org.vitrivr.engine.core.operators.shape

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.NAry] that merges multiple [Operator]s into a single [Flow].
 *
 * <strong>Attention:</strong> The output order of the emitted elements is not defined!
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class MergeOperator<I>(override val inputs: List<Operator<I>>) : Operator.NAry<I, I> {
    override fun toFlow(scope: CoroutineScope): Flow<I> = channelFlow {
        val jobs = this@MergeOperator.inputs.map { p ->
            launch {
                p.toFlow(scope).collect { send(it) }
            }
        }
        jobs.forEach { it.join() }
    }.buffer(128, BufferOverflow.SUSPEND)
}