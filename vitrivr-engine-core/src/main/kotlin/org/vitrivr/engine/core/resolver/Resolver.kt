package org.vitrivr.engine.core.resolver

import org.vitrivr.engine.core.model.retrievable.RetrievableId

/**
 * A [Resolver] resolves a physical resource (e.g., a file) based on a [RetrievableId].
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
interface Resolver {
    /**
     * Attempts to resolve the provided [RetrievableId] to a [Resolvable] using this [Resolver].
     *
     * @param id The [RetrievableId] to resolve.
     * @param suffix The suffix of the filename.
     * @return [Resolvable] or null, if [RetrievableId] could not be resolved.
     */
    fun resolve(id: RetrievableId, suffix: String) : Resolvable?

}