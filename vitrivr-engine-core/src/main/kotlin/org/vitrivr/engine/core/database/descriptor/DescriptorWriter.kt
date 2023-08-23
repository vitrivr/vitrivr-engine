package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.Writer
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [DescriptorReader] is an extension of a [Writer] for [Descriptor]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface DescriptorWriter<T : Descriptor> : Writer<Descriptor> {
    /** The [Schema.Field] this [DescriptorWriter] belongs to. */
    val field: Schema.Field<T>
}