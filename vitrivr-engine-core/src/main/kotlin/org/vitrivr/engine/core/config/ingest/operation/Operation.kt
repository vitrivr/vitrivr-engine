package org.vitrivr.engine.core.config.ingest.operation

import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.operators.transform.shape.MergeType
import java.util.*

/**
 * This sealed class represents a base operation in the ingest pipeline.
 */
sealed class BaseOperation(val name: String, val merge: MergeType?) {

    /** A [LinkedList] of all input [BaseOperation]s. */
    private val _input = LinkedList<BaseOperation>()

    /** A [LinkedList] of all output [BaseOperation]s. */
    private val _output = LinkedList<BaseOperation>()

    /** A [List] of all input [BaseOperation]s. */
    val input: List<BaseOperation>
        get() = Collections.unmodifiableList(this._input)

    /** A [List] of all output [BaseOperation]s. */
    val output: List<BaseOperation>
        get() = Collections.unmodifiableList(this._output)

    /**
     * Adds an input [BaseOperation] to this [BaseOperation].
     *
     * @param operation The [BaseOperation] to add.
     */
    fun addInput(operation: BaseOperation) {
        this._input.add(operation)
        operation.internalAddOutput(this)
    }

    /**
     * Adds an output [BaseOperation] to this [BaseOperation].
     *
     * @param operation The [BaseOperation] to add.
     */
    fun addOutput(operation: BaseOperation) {
        this._output.add(operation)
        operation.internalAddInput(this)
    }

    protected fun internalAddInput(operation: BaseOperation) {
        this._input.add(operation)
    }

    protected fun internalAddOutput(operation: BaseOperation) {
        this._output.add(operation)
    }
}

/**
 * This [Operation] class represents a single operation in the ingest pipeline.
 *
 * @param opName The specific operation name.
 * @param opConfig The configuration for the operation.
 */
class Operation(name: String, val opName: String, val opConfig: OperatorConfig, merge: MergeType? = null) : BaseOperation(name, merge)

/**
 * This [PassthroughOperation] class represents a passthrough operation in the ingest pipeline.
 */
class PassthroughOperation(name: String, merge: MergeType? = null) : BaseOperation(name, merge)
