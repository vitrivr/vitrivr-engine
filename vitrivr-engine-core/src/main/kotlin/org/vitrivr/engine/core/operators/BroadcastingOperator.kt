package org.vitrivr.engine.core.operators

import kotlinx.coroutines.flow.SharedFlow

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
interface BroadcastingOperator<O>: Operator<O> {
    /**
     * Converts this [Operator] to a [SharedFlow] of type [O].
     *
     * The [SharedFlow] allows for broadcasting to downstream [Operator]s.
     *
     * @return Type [O]
     */
    override fun toFlow(): SharedFlow<O>
}