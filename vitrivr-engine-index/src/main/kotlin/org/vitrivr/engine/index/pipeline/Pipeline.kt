package org.vitrivr.engine.index.pipeline

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter

class Pipeline(private val segmenter: Segmenter, private val layout: ExtractorLayout) {


    private val endpoints = layout.layout.map { names ->
        names.foldRight(segmenter as Operator.Unary<*, IngestedRetrievable>) { name, input ->
            layout.schema.getField(name)!!.getExtractor(input)
        }
    }

    suspend fun run() {
        val flows = endpoints.map { it.toFlow() }.toTypedArray()
        terminateFlows(segmenter, *flows){ _, _ ->

        }
    }

    private suspend fun <T> terminateFlows(segmenter: Segmenter, vararg flows: Flow<T>, transform: (List<T>, Int) -> Unit) {
        var counter = 0

        flows.map { flow ->
            flow.map { listOf(it) }
        }.reduceRight { f1, f2 -> f1.combine(f2) { l1, l2 -> l1 + l2 } }.takeWhile {
            ++counter
            !segmenter.inputExhausted || segmenter.emitted > counter
        }.collect {
            transform(it, counter)
        }
    }


}