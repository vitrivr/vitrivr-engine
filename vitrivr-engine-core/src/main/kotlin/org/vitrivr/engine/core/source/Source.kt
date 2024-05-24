package org.vitrivr.engine.core.source

import java.io.File
import java.io.InputStream
import java.util.*

typealias SourceId = UUID

/**
 * A [Source] of data that can be used in an extraction pipeline.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
interface Source {
    /** Unique [SourceId] for this [Source]. */
    val sourceId: SourceId

    /** The name of the [Source]. */
    val name: String

    /** The [MediaType] of the [Source]. */
    val type: MediaType

    /** The timestamp at which this [Source] was generated. */
    val timestamp: Long

    /** Metadata associated with this [Source]. */
    val metadata: MutableMap<String, Any>

    /**
     * Opens a [InputStream] for this [Source]. It remains up to the caller to open the [InputStream].
     *
     * @return [InputStream] for this [Source]
     */
    fun newInputStream(): InputStream

    fun getAsFile(): File

    /**
     * Width of the [Source] in pixels (if applicable and available).
     *
     * Only applicable for [MediaType.IMAGE] and [MediaType.VIDEO].
     *
     * @return [Int]
     */
    fun width(): Int? = this.metadata[Metadata.METADATA_KEY_IMAGE_WIDTH] as? Int

    /**
     * Height of the [Source] in pixels (if applicable and available).
     *
     * Only applicable for [MediaType.IMAGE] and [MediaType.VIDEO].
     * @return [Int]
     */
    fun height(): Int? = this.metadata[Metadata.METADATA_KEY_IMAGE_HEIGHT] as? Int

    /**
     * Frames per second (FPS) of the [Source] in pixels (if applicable and available).
     *
     * Only applicable for [MediaType.VIDEO].
     *
     * @return [Double]
     */
    fun fps(): Double? = this.metadata[Metadata.METADATA_KEY_VIDEO_FPS] as? Double

    /**
     * Number of channels of the [Source] (if applicable and available).
     *
     * Only applicable for [MediaType.VIDEO] and [MediaType.AUDIO].
     *
     * @return [Int]
     */
    fun channels(): Int? = this.metadata[Metadata.METADATA_KEY_AUDIO_CHANNELS] as? Int

    /**
     * Sample rate of the [Source] (if applicable and available).
     *
     * Only applicable for [MediaType.VIDEO] and [MediaType.AUDIO].
     *
     * @return [Double]
     */
    fun sampleRate(): Int? = this.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLERATE] as? Int

    /**
     * Sample size of the [Source] (if applicable and available).
     *
     * Only applicable for [MediaType.VIDEO] and [MediaType.AUDIO].
     *
     * @return [Int]
     */
    fun sampleSize(): Int? = this.metadata[Metadata.METADATA_KEY_AUDIO_SAMPLESIZE] as? Int
}