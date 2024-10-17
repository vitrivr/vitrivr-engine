package org.vitrivr.engine.core.util.extension

import org.vitrivr.engine.core.config.ingest.IngestionPipelineBuilder
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

public enum class Flavor {
    MERMAID
}

fun IngestionPipelineBuilder.build(flavor: Flavor) : List<Operator.Sink<Retrievable>> {
    val nodes = this.build()
    nodes.forEach {
        it.input

    }
    return nodes
}