package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.Reader
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query

/**
 * A [DescriptorReader] is an extension of a [Reader], that allows the execution of [Descriptor] specific [Query] objects.
 *
 * The [DescriptorReader] acts as a shim between the data base layer and vitrivr's data- and query model.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface DescriptorReader<D : Descriptor> : Reader<D> {

    /** The [Analyser] this [DescriptorReader] belongs to. */
    val field: Schema.Field<*,D>

    /**
     * Returns a [Sequence] of all [Descriptor]s accessible by this [DescriptorReader] that match the given [Query].
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [Retrieved] of type [D]
     */
    fun getAll(query: Query<D>): Sequence<Retrieved>
}