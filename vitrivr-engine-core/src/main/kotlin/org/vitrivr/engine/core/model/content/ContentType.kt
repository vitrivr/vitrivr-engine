package org.vitrivr.engine.core.model.content

/**
 * An enumeration of the type of content that can be processed by vitrivr-engine.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
enum class ContentType {
    /** A wrapper for a retrievable id */
    ID,

    /** A scalar value that can be directly mapped to a [org.vitrivr.engine.core.model.descriptor.Descriptor] */
    DESCRIPTOR,

    /** A bitmap image (e.g., extracted from a video or image file). */
    BITMAP_IMAGE,

    /** An audio frame (e.g., extracted from a video or audio file). */
    AUDIO_FRAME,

    /** A 3D mesh. */
    MESH,

    /** Textual contant. */
    TEXT
}