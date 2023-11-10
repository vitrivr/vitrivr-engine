package org.vitrivr.engine.index.config.operators

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Segmenter

/**
 * Configuration element for a [Segmenter] stage in an operator pipeline.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
@Serializable
data class SegmenterConfig(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
    val aggregators: List<AggregatorConfig>
)