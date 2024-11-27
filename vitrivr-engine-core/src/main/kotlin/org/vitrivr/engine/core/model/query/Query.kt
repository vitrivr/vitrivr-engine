package org.vitrivr.engine.core.model.query

/**
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class Query(
    val predicate: Predicate,
    val limit: Long = Long.MAX_VALUE,
)