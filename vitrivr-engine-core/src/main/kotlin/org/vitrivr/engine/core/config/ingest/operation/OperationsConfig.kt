package org.vitrivr.engine.core.config.ingest.operation

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig

/**
 * The [OperationsConfig] describes the ingestion pipeline in the form of [OperatorConfig] names.
 */
@Serializable
data class OperationsConfig (
    val next: String)
