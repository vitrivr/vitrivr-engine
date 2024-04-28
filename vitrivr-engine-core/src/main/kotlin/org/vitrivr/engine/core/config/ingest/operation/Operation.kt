package org.vitrivr.engine.core.config.ingest.operation

import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.operators.transform.shape.MergeType
import java.util.*

/**
 * This [Operation] class represents a single operation in the ingest pipeline.
 *
 * It is used by the [IngestionPipelineBuilder] to build a directed acyclic graph (DAG) of operations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class Operation(val name: String, val opName: String, val opConfig: OperatorConfig, val merge: MergeType? = null) {

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

    /**
     * Finds the root [Operation] of this [Operation].
     *
     * @return Root [Operation].
     */
    fun findRoot(): Operation {
        if (this._input.isEmpty()) {
            return this
        }
        return this._input.first().findRoot()
    }

    /**
     * Finds the terminal base [Operation]s of this [Operation].
     *
     * @return Root [Operation].
     */
    fun findBase(list: MutableList<Operation> = mutableListOf()): List<Operation> {
        if (this._output.isEmpty()) {
            list.add(this)
        } else {
            this._output.forEach { it.findBase(list) }
        }
        return list
    }
}