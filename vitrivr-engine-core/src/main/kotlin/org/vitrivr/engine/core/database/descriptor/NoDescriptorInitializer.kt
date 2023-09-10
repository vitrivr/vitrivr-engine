package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [DescriptorInitializer] that does nothing.
 *
 * Only for testing purposes.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
class NoDescriptorInitializer<D: Descriptor>(override val field: Schema.Field<*,D>) : DescriptorInitializer<D> {
    override fun initialize() { /* No op. */ }

    override fun truncate() { /* No op. */ }
}