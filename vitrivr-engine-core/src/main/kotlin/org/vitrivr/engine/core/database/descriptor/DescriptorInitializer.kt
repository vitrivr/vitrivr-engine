package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [Descriptor] specific extension to the [Initializer] interface.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
interface DescriptorInitializer<D : Descriptor<*>> : Initializer<D> {
    /** The [Analyser] this [DescriptorInitializer] belongs to. */
    val field: Schema.Field<*,D>
}