package org.vitrivr.engine.core.model.content.impl.memory

import org.vitrivr.engine.core.model.content.element.AudioContent
import java.nio.ShortBuffer

/**
 * A naive in-memory implementation of the [AudioContent] interface.
 *
 * Warning: Usage of [InMemoryAudioContent] may lead to out-of-memory situations in large extraction pipelines.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
data class InMemoryAudioContent(override val channels: Short, override val samplingRate: Int, override val content: ShortBuffer) : AudioContent
