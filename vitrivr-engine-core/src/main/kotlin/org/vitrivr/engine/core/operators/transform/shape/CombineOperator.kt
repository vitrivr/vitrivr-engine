package org.vitrivr.engine.core.operators.transform.shape

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * [CombineOperator] merges the results of multiple incoming [Operator]s into a single [Flow]. A [Retrievable] is only emitted,
 * if has been received on every incoming flow.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CombineOperator<T : Retrievable>(override val inputs: List<Operator<T>>, override val name: String = "combine") : Operator.NAry<T, T> {

    /**
     *  Generates a new [channelFlow] that merges the results of multiple incoming [Operator]s into a single [Flow]. Incoming operators are
     *  executed in a new [CoroutineScope] and the order of the [Retrievable]s in the outgoing [Flow] may be arbitrary.
     *
     *  @param scope The [CoroutineScope] of the calling [Operator]
     */
    override fun toFlow(scope: CoroutineScope): Flow<T> = channelFlow {
        val buffer = HashMap<UUID, Pair<T, Int>>()
        val require = this@CombineOperator.inputs.size
        val mutex = Mutex()
        val jobs = this@CombineOperator.inputs.map { p ->
            launch {
                p.toFlow(this).collect { it ->
                    var send: T? = null
                    mutex.lock()
                    val entry = buffer.compute(it.id) { _, v -> if (v == null) it to 1 else v.first to v.second + 1 }
                    if (entry?.second == require) {
                        buffer.remove(it.id)?.first?.let { send = it }
                    }
                    mutex.unlock()
                    send?.let { send(it) }
                }
            }
        }
        jobs.joinAll()
    }
}