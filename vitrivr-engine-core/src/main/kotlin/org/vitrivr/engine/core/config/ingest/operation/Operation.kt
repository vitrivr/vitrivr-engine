package org.vitrivr.engine.core.config.ingest.operation

import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.operators.transform.shape.MergeType
import java.util.*

/**
 * This sealed class represents a base operation in the ingest pipeline.
 */
class Operation(val name: String, val opName: String?, val opConfig: OperatorConfig?, val merge: MergeType?) {

    /** A [LinkedList] of all input [Operation]s. */
    private val _input = LinkedList<Operation>()

    /** A [LinkedList] of all output [Operation]s. */
    private val _output = LinkedList<Operation>()

    /** A [List] of all input [Operation]s. */
    val input: List<Operation>
        get() = Collections.unmodifiableList(this._input)

    /** A [List] of all output [Operation]s. */
    val output: List<Operation>
        get() = Collections.unmodifiableList(this._output)

    /**
     * Adds an input [Operation] to this [Operation].
     *
     * @param operation The [Operation] to add.
     */
    fun addInput(operation: Operation) {
        this._input.add(operation)
        operation._output.add(this)
    }

    /**
     * Adds an output [Operation] to this [Operation].
     *
     * @param operation The [Operation] to add.
     */
    fun addOutput(operation: Operation) {
        this._output.add(operation)
        operation._input.add(this)
    }
}

