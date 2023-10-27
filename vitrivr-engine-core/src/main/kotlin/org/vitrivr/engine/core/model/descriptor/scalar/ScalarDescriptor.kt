package org.vitrivr.engine.core.model.descriptor.scalar

import org.vitrivr.engine.core.model.descriptor.Descriptor

/**
 * A [Descriptor] with a scalar value [T].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
sealed interface ScalarDescriptor<T> : Descriptor {
    val value: T
}
