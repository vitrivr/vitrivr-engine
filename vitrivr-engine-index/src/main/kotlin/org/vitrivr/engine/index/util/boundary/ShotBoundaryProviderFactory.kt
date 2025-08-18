package org.vitrivr.engine.index.util.boundary

import org.vitrivr.engine.core.context.Context

/**
 * A factory object for a specific [ShotBoundaryProvider] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.1.0
 */
interface ShotBoundaryProviderFactory {

    /**
     * Creates and returns a new [ShotBoundaryProvider].
     *
     * @param name The name of the [ShotBoundaryProvider].
     * @param context The [Context] to use with the [ShotBoundaryProvider]
     * @return [ShotBoundaryProvider]
     */
    fun newShotBoundaryProvider(name: String, context: Context): ShotBoundaryProvider
}