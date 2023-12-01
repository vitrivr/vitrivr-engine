package org.vitrivr.engine.core.config.pipeline.execution

/**
 * A status enumeration for [ExecutionServer] jobs.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
enum class ExecutionStatus {
    UNKNOWN,
    RUNNING,
    FAILED,
    COMPLETED
}