package org.vitrivr.engine.core.content.impl

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.InMemoryAudioContent
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.InMemoryTextContent
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

class
InMemoryContentFactory : ContentFactory {

    override fun newImageContent(bufferedImage: BufferedImage): ImageContent = InMemoryImageContent(bufferedImage)

    override fun newAudioContent(channel: Int, samplingRate: Int, audio: ShortBuffer): AudioContent = InMemoryAudioContent(channel, samplingRate, audio)

    override fun newTextContent(text: String): TextContent = InMemoryTextContent(text)
}