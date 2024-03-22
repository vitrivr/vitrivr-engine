package org.vitrivr.engine.core.model.query.fulltext

import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.types.Value


/**
 * A [SimpleFulltextQuery] that uses a [Value.String] to execute fulltext search on the underlying field.
 *
 * @author Ralph Gasser
 * @version 2.0.0
 */
data class SimpleFulltextQuery(
    /** The [Value] being used for the query. */
    val value: Value.String,

    /**
     * The name of the attribute that should be compared.
     *
     * Typically, this is pre-determined by the analyser. However, in some cases, this must be specified (e.g., when querying struct fields).
     */
    val attributeName: String? = null,

    /** The number of results that should be returned by this [SimpleBooleanQuery]. */
    val limit: Long = Long.MAX_VALUE
) : Query
