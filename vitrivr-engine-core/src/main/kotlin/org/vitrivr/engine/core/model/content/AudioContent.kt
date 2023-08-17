package org.vitrivr.engine.core.model.content

import java.nio.ByteBuffer

/**
 * A aural [Content] element.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface AudioContent: Content {
    /** The sampling rate of the data encoded in this [AudioContent]. */
    val samplingRate: Int

    /** The number of samples encoded in this [AudioContent]. */
    val numberOfChannels: Int

    /** The number of samples encoded in this [AudioContent]. */
    val numberOfSamples: Int

    /** The raw audio data [ByteBuffer] associated with this [AudioContent]. */
    val samples: ByteBuffer
}