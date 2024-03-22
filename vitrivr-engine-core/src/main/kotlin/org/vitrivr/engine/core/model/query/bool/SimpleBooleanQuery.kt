package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.types.Value

/**
 * A [SimpleBooleanQuery] that compares a field to a [Value] of type [T] using the given [ComparisonOperator].
 *
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
data class SimpleBooleanQuery<T : Value<*>>(
    /** The [Value] being used; specifies both the query field and the comparison value. */
    val value: T,

    /** The [ComparisonOperator] to employ. */
    val comparison: ComparisonOperator = ComparisonOperator.EQ,

    /**
     * The name of the attribute that should be compared.
     *
     * Typically, this is pre-determined by the analyser. However, in some cases, this must be specified (e.g., when querying struct fields).
     */
    val attributeName: String? = null,

    /** The number of results that should be returned by this [SimpleBooleanQuery]. */
    val limit: Long = Long.MAX_VALUE
) : Query