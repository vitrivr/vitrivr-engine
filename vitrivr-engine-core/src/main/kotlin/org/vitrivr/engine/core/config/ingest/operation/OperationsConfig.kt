package org.vitrivr.engine.core.config.ingest.operation

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.config.pipeline.execution.IndexingPipeline

/**
 * The [OperationsConfig] describes the ingestion pipeline in the form of [OperatorConfig] names.
 *
 * Currently, this is simply an ordered list (see [IndexingPipelinie].
 * In the future there might be cases where a tree (or graph) structure is desirable
 */
@Serializable
data class OperationsConfig (
    /**
     * The name of te [OperatorConfig] at this stage.
     */
    val operator: String

)
