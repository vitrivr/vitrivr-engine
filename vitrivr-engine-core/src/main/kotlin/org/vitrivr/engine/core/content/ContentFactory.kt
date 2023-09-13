package org.vitrivr.engine.core.content

import org.vitrivr.engine.core.model.content.AudioContent
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.content.TextContent
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

interface ContentFactory {

    fun newImageContent(bufferedImage: BufferedImage): ImageContent

    fun newAudioContent(channel: Int, samplingRate: Int, audio: ShortBuffer): AudioContent

    fun newTextContent(text: String): TextContent

}