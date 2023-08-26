package org.vitrivr.engine.core.util.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import org.vitrivr.engine.core.operators.ingest.Segmenter

suspend fun <T> Segmenter.terminateFlows(vararg flows: Flow<T>, transform: (List<T>, Int) -> Unit) {
    var counter = 0

    flows.map { flow ->
        flow.map { listOf(it) }
    }.reduce { f1, f2 -> f1.combine(f2) { l1, l2 -> l1 + l2 } }.takeWhile {
        ++counter
        !this.inputExhausted || this.emitted > counter
    }.collect {
        transform(it, counter)
    }
}