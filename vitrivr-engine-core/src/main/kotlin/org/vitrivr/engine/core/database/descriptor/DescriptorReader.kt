package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.Reader
import org.vitrivr.engine.core.model.database.Persistable
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.operators.Describer
import org.vitrivr.engine.core.operators.DescriberId

/**
 * A [DescriptorReader] is an extension of a [Reader], that allows the execution of [Descriptor] specific [Query] objects.
 *
 * The [DescriptorReader] acts as a shim between the data base layer and vitrivr's data- and query model.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface DescriptorReader<T : Descriptor> : Reader<T> {

    /** The [Describer] this [DescriptorReader] belongs to. */
    val describer: Describer<T>

    /**
     * Returns a [Sequence] of all [Descriptor]s accessible by this [DescriptorReader] that match the given [Query].
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [Persistable] of type [T]
     */
    fun getAll(query: Query<T>): Sequence<T>
}