package org.vitrivr.engine.core.operators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * A basic implementation of a pipeline-able operator.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
sealed interface Operator<O> {


    /** The name of this [Operator]. In case a field is set, is equivalent to field.fieldName.*/
    val name: String

    /**
     * Returns root of this [Unary].
     */
    fun root(): Operator<*>

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
    interface Nullary<I>: Operator<I> {
        /**
         * Returns root of this [Nullary] (which is this).
         */
        override fun root(): Operator<*> = this
    }

    /**
     * A unary [Operator] with one input [Operator] and one output [Operator].
     */
    interface Unary<I,O>: Operator<O> {
        /** The input [Operator]. */
        val input: Operator<out I>

        /**
         * Returns root of this [Unary].
         */
        override fun root(): Operator<*> = this.input.root()
    }

    /**
     * A binary [Operator] with two inputs [Operator] and one output [Operator].
     */
    interface Binary<I,O>: Operator<O> {
        /** The input [Operator]. */
        val input1: Operator<out I>

        /** The input [Operator]. */
        val input2: Operator<out I>

        /**
         * Returns root of this [Unary], which is the left-hand operator (by definition).
         */
        override fun root(): Operator<*> = this.input1.root()
    }

    /**
     * An N-ary [Operator] with two inputs [Operator] and one output [Operator].
     */
    interface NAry<I,O>: Operator<O> {
        /** The input [Operator]s. */
        val inputs: List<Operator<out I>>

        /**
         * Returns root of this [Unary], which is the left-hand operator (by definition).
         */
        override fun root(): Operator<*> = this.inputs.first().root()
    }

    /**
     * A [Sink] is an [Operator] that has no output.
     */
    interface Sink<I>: Operator<Unit> {
        /** The input [Operator]. */
        val input: Operator<out I>

        /**
         * Returns root of this [Unary], which is the left-hand operator (by definition).
         */
        override fun root(): Operator<*> = this.input.root()
    }
}