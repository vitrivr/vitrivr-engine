package org.vitrivr.engine.index.util.boundary

/**
 * An interface that defines a method for decoding media segment boundaries.
 * Implementations of this interface are used by [ShotBoundaryProviderFactory] to create instances of [ShotBoundaryProvider].
 *
 * @author Raphael Waltenspühl
 * @version 1.1.0
 */

interface ShotBoundaryProvider {
    /**
     * Decodes media segment boundaries from the provided source URI and returns a list of [MediaSegmentDescriptor] objects.
     *
     * @param assetUri The URI of the media source to decode
     * @return A list of [MediaSegmentDescriptor] objects representing the decoded media segment boundaries
     */
    fun decode(assetUri: String): List<MediaSegmentDescriptor>
}
