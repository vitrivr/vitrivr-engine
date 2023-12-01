package org.vitrivr.engine.server.api.rest.model

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionStatus

/**
 * Data class indicating the status of an ingest job.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Serializable
data class IngestStatus(val jobId: String, val executionStatus: ExecutionStatus, val timestamp: Long = System.currentTimeMillis())