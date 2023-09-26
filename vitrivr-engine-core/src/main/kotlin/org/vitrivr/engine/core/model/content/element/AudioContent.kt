package org.vitrivr.engine.core.model.content.element

import java.nio.ShortBuffer

/**
 * A aural [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface AudioContent: ContentElement<ShortBuffer> {
    /** The number of samples encoded in this [AudioContent]. */
    val channel: Int

    /** The sampling rate of the data encoded in this [AudioContent]. */
    val samplingRate: Int

    /**
     * Returns the number of samples encoded in this [AudioContent].
     *
     * @return Number of samples
     */
    fun getNumberOfSamples(): Int = this.getContent().limit()
}