package org.vitrivr.engine.core.model.query.proximity

import org.vitrivr.engine.core.model.database.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.SortOrder

/**
 * A [ProximityQuery] that uses a [VectorDescriptor] of type [T].
 *
 * A [ProximityQuery] is typically translated to a neighbour search, comparing the field specified by
 * the [VectorDescriptor] and  the value specified by the [VectorDescriptor] returning [k] nearest or
 * farthest neighbours (depending on the [order]) under the specified [Distance].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */

data class ProximityQuery<T: VectorDescriptor<*>>(
    /** The [VectorDescriptor] being used; specifies both the query field and the comparison value. */
    override val descriptor: T,

    /** The number of results that should be returned by this [ProximityQuery]. */
    val k: Int = 1000,

    /** The desired [SortOrder] of the results. Influences, whether the nearest or farthest neighbours are returned. */
    val order: SortOrder = SortOrder.ASC,

    /** The [Distance] used for the comparison. */
    val distance: Distance = Distance.EUCLIDEAN,

    val returnDescriptor: Boolean = false
) : Query<T>