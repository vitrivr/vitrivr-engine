package org.vitrivr.engine.core.database.descriptor

import org.vitrivr.engine.core.database.Reader
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.DescriptorId
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved

/**
 * A [DescriptorReader] is an extension of a [Reader], that allows the execution of [Descriptor] specific [Query] objects.
 *
 * The [DescriptorReader] acts as a shim between the data base layer and vitrivr's data- and query model.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 2.0.1
 */
interface DescriptorReader<D : Descriptor<*>> : Reader<D> {

    /** The [Analyser] this [DescriptorReader] belongs to. */
    val field: Schema.Field<*,D>

    /**
     * Checks if a [Descriptor] of type [D] for provided [DescriptorId] exists.
     *
     * @param descriptorId The [DescriptorId] to check for.
     * @return True if [Retrievable] exists, false otherwise.
     */
    fun exists(descriptorId: DescriptorId): Boolean

    /**
     * Returns the [Descriptor] of type [D]  that corresponds to the provided [DescriptorId].
     *
     * @param descriptorId The [DescriptorId] to return.
     * @return [Descriptor] of type [D] or null
     */
    operator fun get(descriptorId: DescriptorId): D?

    /**
     * Returns a [Sequence] of all [Descriptor] whose [DescriptorId] is contained in the provided [Iterable].
     *
     * @param descriptorIds A [Iterable] of [DescriptorId]s to return.
     * @return [Sequence] of [Descriptor] of type [D]
     */
    fun getAll(descriptorIds: Iterable<DescriptorId>): Sequence<D>

    /**
     * Returns the [Descriptor]s of type [D] that belong to the provided [RetrievableId].
     *
     * @param retrievableId The [RetrievableId] to search for.
     * @return [Sequence] of [Descriptor]  of type [D]
     */
    fun getForRetrievable(retrievableId: RetrievableId): Sequence<D>

    /**
     * Returns a [Sequence] of all [Descriptor]  whose [RetrievableId] is contained in the provided [Iterable].
     *
     * @param retrievableIds A [Iterable] of [RetrievableId]s to return [Descriptor]s for
     * @return [Sequence] of [Descriptor] of type [D]
     */
    fun getAllForRetrievable(retrievableIds: Iterable<RetrievableId>): Sequence<D>

    /**
     * Returns a [Sequence] of all [Descriptor]s [D]s that match the given [Query].
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [D].
     */
    fun query(query: Query): Sequence<D>

    /**
     * Returns a [Sequence] of all [Retrieved]s that match the given [Query].
     *
     * Implicitly, this methods executes a [query] and then JOINS the result with the [Retrieved]s.
     *
     * @param query The [Query] that should be executed.
     * @return [Sequence] of [Retrieved].
     */
    fun queryAndJoin(query: Query): Sequence<Retrieved>
}