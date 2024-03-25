package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.query.Query

/**
 * A common interface for all [BooleanQuery] types.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface BooleanQuery : Query {
    /** The number of results that should be returned by this [BooleanQuery]. */
    val limit: Long
}