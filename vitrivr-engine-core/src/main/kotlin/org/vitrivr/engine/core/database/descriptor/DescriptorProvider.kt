package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.Connection
import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Field

/**
 * A helper class that provides [DescriptorInitializer], [DescriptorWriter] and [DescriptorReader] instances for a specific [Descriptor].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface DescriptorProvider<T : Descriptor> {
    /**
     * Returns a [DescriptorInitializer].
     *
     * @param connection The [Connection] to return [DescriptorInitializer] for.
     * @param field The [Field] to return a new [DescriptorInitializer] for.
     * @return New [DescriptorInitializer] instance.
     */
    fun newInitializer(connection: Connection, field: Field<T>): DescriptorInitializer<T>

    /**
     * Returns a [DescriptorReader].
     *
     * @param connection The [Connection] to return [DescriptorReader] for.
     * @param field The [Field] to return a new [DescriptorReader] for.
     * @return New [DescriptorReader] instance.
     */
    fun newReader(connection: Connection, field: Field<T>): DescriptorReader<T>

    /**
     * Returns a [DescriptorWriter].
     *
     * @param connection The [Connection] to return [DescriptorWriter] for.
     * @param field The [Field] to return a new [DescriptorReader] for.
     * @return New [DescriptorWriter] instance.
     */
    fun newWriter(connection: Connection, field: Field<T>): DescriptorWriter<T>
}