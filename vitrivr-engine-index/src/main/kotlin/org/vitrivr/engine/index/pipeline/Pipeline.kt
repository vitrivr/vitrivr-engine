package org.vitrivr.engine.index.pipeline

import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.util.extension.terminateFlows

class Pipeline(private val segmenter: Segmenter, private val layout: ExtractorLayout) {


    private val endpoints = layout.layout.map { options ->
        options.foldRight(segmenter as Operator.Unary<*, IngestedRetrievable>) { option, input ->
            layout.schema.getField(option.name)!!.getExtractor(input, option.persisting)
        }
    }

    suspend fun run(collectTransform: (List<IngestedRetrievable>, Int) -> Unit = {_, _ -> }) {
        val flows = endpoints.map { it.toFlow() }.toTypedArray()
        segmenter.terminateFlows(*flows, transform = collectTransform)
    }

}