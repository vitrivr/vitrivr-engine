package org.vitrivr.engine.core.model.retrievable.decorators

import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * A [Retrievable] that has [Descriptor] elements attached to it.
 *
 * Used during ingesting and indexing and for certain types of queries.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithDescriptor : Retrievable {
    /** List of [Descriptor] elements that are stored for this [RetrievableWithDescriptor]. */
    val descriptors: List<Descriptor>

    /**
     * Returns the number of [Descriptor] elements for this [RetrievableWithContent].
     *
     * @return Number of [Descriptor] elements for this [RetrievableWithContent].
     */
    fun descriptorSize(): Int = this.descriptors.size

    /**
     * Gets the [Descriptor] at the provided [index].
     *
     * @param index The index of the [Descriptor] to return.
     * @return [Content]
     */
    fun getDescriptor(index: Int): Descriptor = this.descriptors[index]

    /**
     * A [Mutable] version of the [RetrievableWithDescriptor].
     */
    interface Mutable : RetrievableWithDescriptor {
        /**
         * Adds a [Descriptor] to this [RetrievableWithDescriptor].
         *
         * @param descriptor The [Descriptor] element to add.
         */
        fun addDescriptor(descriptor: Descriptor)

        /**
         * Clears list of descriptors.
         */
        fun clearDescriptors()
    }
}