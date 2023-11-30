package org.vitrivr.engine.core.config.pipeline.execution

import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * A pipeline for indexing. It wraps a [List] of [Operator]s.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class IndexingPipeline {
    private val leaves: MutableList<Operator<Retrievable>> = mutableListOf()

    fun addLeaf(leaf: Operator<Retrievable>) {
        this.leaves.add(leaf)
    }

    fun getLeaves(): List<Operator<Retrievable>> {
        return Collections.unmodifiableList(this.leaves)
    }
}