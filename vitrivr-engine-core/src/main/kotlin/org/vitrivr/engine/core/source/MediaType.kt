package org.vitrivr.engine.core.source

/**
 * An enumeration of the supported [MediaType]s.
 *
 * @author Luca Rossetto
 * @version 1.0.0
 */
enum class MediaType {
    /** A still image. */
    IMAGE,

    /** A pure audio stream. */
    AUDIO,

    /** A video(containing an image, audio, and potentially a textual stream). */
    VIDEO,

    /** A 3D mesh. */
    MESH
}