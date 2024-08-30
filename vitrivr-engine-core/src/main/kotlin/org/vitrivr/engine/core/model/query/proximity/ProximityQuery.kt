package org.vitrivr.engine.core.model.query.proximity

import org.vitrivr.engine.core.model.descriptor.vector.VectorDescriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.SortOrder
import org.vitrivr.engine.core.model.types.Value

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

data class ProximityQuery<T : Value.Vector<*>>(
    /** The [VectorDescriptor] being used; specifies both the query field and the comparison value. */
    val value: T,

    /** The [Distance] used for the comparison. */
    val distance: Distance = Distance.EUCLIDEAN,

    /** The desired [SortOrder] of the results. Influences, whether the nearest or farthest neighbours are returned. */
    val order: SortOrder = SortOrder.ASC,

    /** The number of results that should be returned by this [ProximityQuery]. */
    val k: Long = 1000L,

    /** Flag indicating, whether [VectorDescriptor] should be returned as well. */
    val fetchVector: Boolean = false,

    /**
     * The name of the attribute that should be compared.
     *
     * Typically, this is pre-determined by the analyser. However, in some cases, this must be specified (e.g., when querying struct fields).
     */
    val attributeName: String? = null
) : Query