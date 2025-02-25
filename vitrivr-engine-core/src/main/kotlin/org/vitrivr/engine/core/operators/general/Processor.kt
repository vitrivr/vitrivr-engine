package org.vitrivr.engine.core.operators.general

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that processes the elements of a [Flow] without changing them.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class Processor(override val name: String, override val input: Operator<out Retrievable>): Operator.Unary<Retrievable, Retrievable> {
    /**
     * Converts this [Processor] to a [Flow]
     *
     * @param scope The [CoroutineScope] to use.
     * @return [Flow]
     */
    final override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this@Processor.input.toFlow(scope).onEach { retrievable ->
        process(retrievable)
    }

    /**
     * Processes this [Retrievable], without changing it.
     *
     * @param retrievable The [Retrievable] to process.
     */
    abstract fun process(retrievable: Retrievable)
}