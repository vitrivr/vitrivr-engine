package org.vitrivr.engine.core.model.query.bool

import org.vitrivr.engine.core.model.database.descriptor.scalar.ScalarDescriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator

/**
 * A [BooleanQuery] that uses a [ScalarDescriptor] of type [T].
 *
 * A [BooleanQuery] is typically translated to comparison between the field specified by the [ScalarDescriptor] and
 * the value specified by the [ScalarDescriptor] using the given [ComparisonOperator].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class BooleanQuery<T>(
    /** The [ScalarDescriptor] being used; specifies both the query field and the comparison value. */
    override val descriptor: ScalarDescriptor<T>,

    /** The [ComparisonOperator] to employ. */
    val comparison: ComparisonOperator = ComparisonOperator.EQ
): Query<ScalarDescriptor<T>>