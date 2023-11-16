package org.vitrivr.engine.core.config.pipeline

import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.Source
import java.util.*

open class Pipeline() {
    private val leaves: MutableList<Operator<Retrievable>> = mutableListOf()

    fun addLeaf(leaf: Operator<Retrievable>) {
        this.leaves.add(leaf)
    }

    fun getLeaves(): List<Operator<Retrievable>> {
        return Collections.unmodifiableList(this.leaves)
    }
}