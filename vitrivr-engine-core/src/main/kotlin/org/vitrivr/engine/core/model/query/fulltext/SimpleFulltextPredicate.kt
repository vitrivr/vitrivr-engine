package org.vitrivr.engine.core.model.query.fulltext

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Predicate
import org.vitrivr.engine.core.model.query.bool.BooleanPredicate
import org.vitrivr.engine.core.model.types.Value


/**
 * A [SimpleFulltextPredicate] that uses a [Value.String] to execute fulltext search on the underlying field.
 *
 * @author Ralph Gasser
 * @version 2.2.0
 */
data class SimpleFulltextPredicate(
    /** The [Schema.Field] that this [Predicate] is applied to. */
    val field: Schema.Field<*, *>,

    /** The [Value] being used for the query. */
    val value: Value.Text,

    /**
     * The name of the attribute that should be compared.
     *
     * Typically, this is pre-determined by the analyser. However, in some cases, this must be specified (e.g., when querying struct fields).
     */
    val attributeName: String? = null,

    /** Optional filter query for this [SimpleFulltextPredicate]. */
    val filter: BooleanPredicate? = null
) : Predicate
