package org.vitrivr.engine.core.config.operators

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Segmenter

/**
 * Configuration element for a [Segmenter] stage in an operator pipeline.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
@Serializable
@Deprecated(
    message = "Replaced by the new extraction pipeline definition language in package package org.vitrivr.engine.core.config.ingest",
    replaceWith = ReplaceWith("org.vitrivr.engine.core.config.ingest.operator.SegmenterConfig"),
    level = DeprecationLevel.WARNING
)
data class SegmenterConfig(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
    val aggregators: List<AggregatorConfig>
)
