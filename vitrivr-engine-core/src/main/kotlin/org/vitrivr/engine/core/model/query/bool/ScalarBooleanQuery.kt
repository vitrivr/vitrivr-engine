package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator

/**
 * A [ScalarBooleanQuery] that uses a [ScalarDescriptor] of type [T].
 *
 * A [ScalarBooleanQuery] is typically translated to comparison between the field specified by the [ScalarDescriptor] and
 * the value specified by the [ScalarDescriptor] using the given [ComparisonOperator].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class ScalarBooleanQuery<T : ScalarDescriptor<*>>(
    /** The [ScalarDescriptor] being used; specifies both the query field and the comparison value. */
    override val descriptor: T,
    /** The [ComparisonOperator] to employ */
    override val comparison: ComparisonOperator = ComparisonOperator.EQ,
    ) : SimpleBooleanQuery<T>
