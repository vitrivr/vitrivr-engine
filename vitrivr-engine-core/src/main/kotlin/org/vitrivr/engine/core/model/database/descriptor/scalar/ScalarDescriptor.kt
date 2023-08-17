package org.vitrivr.engine.core.model.database.descriptor.scalar

import org.vitrivr.engine.core.model.database.descriptor.Descriptor

/**
 * A [Descriptor] with a scalar value [T].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface ScalarDescriptor<T> : Descriptor {
    val value: T
}
