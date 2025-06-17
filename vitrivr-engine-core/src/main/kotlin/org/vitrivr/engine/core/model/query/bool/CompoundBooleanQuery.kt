package org.vitrivr.engine.core.model.query.bool

/**
 * A [CompoundBooleanQuery] that combines multiple [BooleanQuery] instances with a logical AND operation.
 * This allows for more complex queries where multiple conditions must be satisfied simultaneously.
 *
 * @author henrikluemkemann
 * @version 1.0.0
 */
data class CompoundBooleanQuery(
    /** The list of [BooleanQuery] instances to be combined with AND. */
    val queries: List<BooleanQuery>,
    
    /** The number of results that should be returned by this [CompoundBooleanQuery]. */
    override val limit: Long = Long.MAX_VALUE
) : BooleanQuery {}