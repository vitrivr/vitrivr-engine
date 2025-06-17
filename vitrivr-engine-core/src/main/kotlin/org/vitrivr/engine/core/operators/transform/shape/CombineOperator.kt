package org.vitrivr.engine.core.operators.transform.shape

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.Operator

/**
 * [CombineOperator] merges the results of multiple incoming [Operator]s into a single [Flow].
 *
 * A [Retrievable] is only emitted, if has been received on every incoming flow. The emitted [Retrievable] will contain
 * a combination of all incoming [Retrievable]s.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class CombineOperator(override val inputs: List<Operator<Retrievable>>, override val name: String = "combine") : Operator.NAry<Retrievable, Retrievable> {

    /**
     *  Generates a new [channelFlow] that merges the results of multiple incoming [Operator]s into a single [Flow]. Incoming operators are
     *  executed in a new [CoroutineScope] and the order of the [Retrievable]s in the outgoing [Flow] may be arbitrary.
     *
     *  @param scope The [CoroutineScope] of the calling [Operator]
     */
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = channelFlow {
        val buffer = HashMap<RetrievableId, MutableList<Retrievable>>()
        val require = this@CombineOperator.inputs.size
        val mutex = Mutex()
        val jobs = this@CombineOperator.inputs.map { p ->
            launch {
                p.toFlow(this).collect { it ->
                    var send: Retrievable? = null
                    mutex.lock()
                    val retrievables = buffer.compute(it.id) { _, v -> v?.apply { add(it) } ?: mutableListOf(it) }!!
                    if (retrievables.size == require) {
                        val attributes = retrievables.flatMap { it.attributes }.distinct()
                        val content = retrievables.flatMap { it.content }.distinct()
                        val descriptors = retrievables.flatMap { it.descriptors }.distinct()
                        val relationship = retrievables.flatMap { it.relationships }.distinct()
                        send = it.copy(content = it.content + content, descriptors = it.descriptors + descriptors, attributes = it.attributes + attributes, relationships = it.relationships + relationship)
                        buffer.remove(it.id)
                    }
                    mutex.unlock()
                    send?.let {
                        send(it)
                    }
                }
            }
        }
        jobs.joinAll()
    }
}