package org.vitrivr.engine.core.operators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * A basic implementation of a pipeline-able operator.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface Operator<O> {

    /**
     * Converts this [Operator] to a [Flow] of type [O].
     *
     * @param scope The [CoroutineScope] to execute the [Flow] in.
     * @return Type [O]
     */
    fun toFlow(scope: CoroutineScope): Flow<O>

    /**
     * A nullary operator (usually a source).
     */
    interface Nullary<O>: Operator<O>

    /**
     * A unary [Operator] with one input [Operator] and one output [Operator].
     */
    interface Unary<I,O>: Operator<O> {
        /** The input [Operator]. */
        val input: Operator<I>
    }

    /**
     * A binary [Operator] with two inputs [Operator] and one output [Operator].
     */
    interface Binary<I,O>: Operator<O> {
        /** The input [Operator]. */
        val input1: Operator<I>

        /** The input [Operator]. */
        val input2: Operator<I>
    }

    /**
     * An N-ary [Operator] with two inputs [Operator] and one output [Operator].
     */
    interface NAry<I,O>: Operator<O> {
        /** The input [Operator]s. */
        val inputs: List<Operator<I>>
    }

    /**
     * A [Sink] is an [Operator] that has no output.
     */
    interface Sink<I>: Operator<Unit> {
        /** The input [Operator]. */
        val input: Operator<I>
    }
}