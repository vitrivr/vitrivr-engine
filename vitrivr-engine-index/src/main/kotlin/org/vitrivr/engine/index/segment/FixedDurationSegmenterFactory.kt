package org.vitrivr.engine.index.segment

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.SegmenterFactory

import java.time.Duration

/**
 * A [SegmenterFactory] for the [FixedDurationSegmenter].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FixedDurationSegmenterFactory : SegmenterFactory {
    override fun newOperator(input: Operator<ContentElement<*>>, parameters: Map<String, Any>, schema: Schema, context: Context): FixedDurationSegmenter {
        val retrievableWriter = schema.connection.getRetrievableWriter()
        val duration = Duration.ofSeconds(
            (parameters["duration"] as String? ?: throw IllegalArgumentException("'duration' must be specified")).toLong()
        )
        val lookAheadTime = Duration.ofSeconds(
            (parameters["duration"] as String? ?: throw IllegalArgumentException("'lookAheadTime' must be specified")).toLong()
        )
        return FixedDurationSegmenter(input, retrievableWriter, duration, lookAheadTime)
    }
}