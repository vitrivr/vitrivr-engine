package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator

/**
 * A [SimpleBooleanQuery] for the descriptor [T].
 *
 * Boolean queries have in common, that they are usually comparative.
 */
interface SimpleBooleanQuery<T : Descriptor> : Query<T> {
    /** The [ComparisonOperator] to employ. */
    val comparison: ComparisonOperator
}
