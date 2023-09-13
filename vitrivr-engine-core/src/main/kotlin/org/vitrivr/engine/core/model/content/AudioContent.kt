package org.vitrivr.engine.core.model.content

import java.nio.ByteBuffer
import java.nio.ShortBuffer

/**
 * A aural [Content] element.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface AudioContent: Content<ShortBuffer> {
    /** The number of samples encoded in this [AudioContent]. */
    val channel: Int

    /** The sampling rate of the data encoded in this [AudioContent]. */
    val samplingRate: Int

    /**
     * Returns the [ByteBuffer] representing the samples in this [AudioContent].
     *
     * @return [ByteBuffer] containing the audio samples.
     */
    override fun getContent(): ShortBuffer

    /**
     * Returns the number of samples encoded in this [AudioContent].
     *
     * @return Number of samples
     */
    fun numberOfSamples(): Int = this.getContent().limit()
}