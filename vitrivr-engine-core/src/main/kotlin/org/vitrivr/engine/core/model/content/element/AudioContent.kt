package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import java.nio.ShortBuffer

/**
 * A aural [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface AudioContent: ContentElement<ShortBuffer> {
    /** Number of samples encoded in this [AudioContent]. */
    val samples: Int
        get() = this.content.limit() / this.channels

    /** The size of this [AudioContent] in bytes. */
    val size: Int
        get() = this.content.limit() * Short.SIZE_BYTES

    /** The number of channels encoded in this [AudioContent]. */
    val channels: Short

    /** The sampling rate of the data encoded in this [AudioContent]. */
    val samplingRate: Int

    /** The [ContentType] of an [AudioContent] is always [ContentType.AUDIO_FRAME]. */
    override val type: ContentType
        get() = ContentType.AUDIO_FRAME
}

