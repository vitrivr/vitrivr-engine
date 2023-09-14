package org.vitrivr.engine.core.content

import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

interface ContentFactory {

    fun newImageContent(bufferedImage: BufferedImage): ImageContent

    fun newAudioContent(channel: Int, samplingRate: Int, audio: ShortBuffer): AudioContent

    fun newTextContent(text: String): TextContent

}