package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [Descriptor] specific extension to the [Initializer] interface.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
interface DescriptorInitializer<T : Descriptor> : Initializer<T> {
    /** The [Analyser] this [DescriptorInitializer] belongs to. */
    val field: Schema.Field<T>
}