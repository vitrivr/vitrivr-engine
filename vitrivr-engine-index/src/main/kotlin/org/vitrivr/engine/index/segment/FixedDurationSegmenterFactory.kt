package org.vitrivr.engine.index.segment

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.operators.ingest.SegmenterFactory
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.index.transform.PassthroughTransformer
import java.time.Duration

class FixedDurationSegmenterFactory : SegmenterFactory {
    override fun newOperator(
        input: Operator<ContentElement<*>>,
        parameters: Map<String, Any>,
        schema: Schema
    ): FixedDurationSegmenter {

        val retrievableWriter = schema.connection.getRetrievableWriter()
        val duration = Duration.ofSeconds(parameters["duration"] as Long)
            ?: throw IllegalArgumentException("Duration must be specified")
        val lookAheadTime = Duration.ofSeconds(parameters["lookAheadTime"] as Long)
            ?: throw IllegalArgumentException("Duration must be specified")

        return FixedDurationSegmenter(input, retrievableWriter, duration, lookAheadTime)
    }
}