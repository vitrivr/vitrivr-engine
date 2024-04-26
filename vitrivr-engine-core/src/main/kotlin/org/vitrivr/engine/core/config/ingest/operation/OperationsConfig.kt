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
     * The name of the [OperatorConfig] at this stage.
     * Must be a name of the [IngestionConfig.operators] property.
     */
    val operator: String,

    /**
     * The names of the [OperationsConfig] that follow this operation.
     * Must be a name of the [IngestionConfig.operations] property.
     */
    val inputs: List<String> = emptyList()

){
    /**
     * Indicates whether this [OperationsConfig] is an entry point, e.g. has no [inputs].
     *
     * @return TRUE if no [inputs] are defined, FALSE otherwise.
     */
    fun isEntry(): Boolean {
        return inputs.isEmpty()
    }
}
