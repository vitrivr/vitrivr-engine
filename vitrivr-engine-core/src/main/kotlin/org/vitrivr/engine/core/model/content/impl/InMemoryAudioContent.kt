package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.AudioContent
import java.nio.ShortBuffer

/**
 * A naive in-memory implementation of the [AudioContent] interface.
 *
 * Warning: Usage of [InMemoryAudioContent] may lead to out-of-memory situations in large extraction pipelines.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
internal data class InMemoryAudioContent(override val channel: Int, override val samplingRate: Int, private val audio: ShortBuffer): AudioContent {
    override fun getContent(): ShortBuffer = this.audio
}
