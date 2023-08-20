package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.operators.Describer
import org.vitrivr.engine.core.operators.DescriberId

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
     * @param describer The [Describer] to return a new [DescriptorInitializer] for.
     * @return New [DescriptorInitializer] instance.
     */
    fun newInitializer(describer: Describer<T>): DescriptorInitializer<T>

    /**
     * Returns a [DescriptorReader].
     *
     * @param describer The [Describer] to return a new [DescriptorReader] for.
     * @return New [DescriptorReader] instance.
     */
    fun newReader(describer: Describer<T>): DescriptorReader<T>

    /**
     * Returns a [DescriptorWriter].
     *
     * @param describer The [DescriberId] to return a new [DescriptorWriter] for.
     * @return New [DescriptorWriter] instance.
     */
    fun newWriter(describer: Describer<T>): DescriptorWriter<T>
}