package org.vitrivr.engine.index.util.boundaryFile

import org.vitrivr.engine.core.context.Context



/**
 * A factory object for a specific [ShotBoundaryProvider] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface ShotBoundaryProviderFactory {
    fun newShotBoundaryProvider(name: String, context: Context): ShotBoundaryProvider
}

interface ShotBoundaryProvider {
    fun decode(assetUri: String): List<MediaSegmentDescriptor>
}
