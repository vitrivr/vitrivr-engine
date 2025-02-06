package org.vitrivr.engine.core.operators.sinks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.Sink] that does not do anything with the input.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class SilentSink(override val name: String = "Silent", override val input: Operator<out Retrievable>) : Operator.Sink<Retrievable> {
    override fun toFlow(scope: CoroutineScope): Flow<Unit> = flow {
        input.toFlow(scope).collect()
    }
}