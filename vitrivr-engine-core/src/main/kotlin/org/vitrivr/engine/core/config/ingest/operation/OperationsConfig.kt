package org.vitrivr.engine.core.config.ingest.operation

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.config.ingest.IngestionConfig
import org.vitrivr.engine.core.config.ingest.IngestionPipelineBuilder
import org.vitrivr.engine.core.config.pipeline.execution.IndexingPipeline

/**
 * The [OperationsConfig] describes the ingestion pipeline in the form of [OperatorConfig] names.
 *
 * Currently, this is simply an ordered list (see [IndexingPipeline].
 *
 * In the future there might be cases where a tree (or graph) structure is desirable and explicitly
 * the results of one operator can be passed to the next one.
 *
 * @see IngestionConfig
 * @see IngestionPipelineBuilder
 */
@Serializable
data class OperationsConfig (
    /**
     * The name of te [OperatorConfig] at this stage.
     */
    val operator: String

)
