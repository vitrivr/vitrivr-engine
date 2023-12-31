package org.vitrivr.engine.core.model.query

import org.vitrivr.engine.core.model.descriptor.Descriptor

/**
 * A [Query] that can be executed by vitrivr's retrieval pipeline.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Query<T : Descriptor> {

    /** The [Descriptor] used by this [Query]. */
    val descriptor: T
}