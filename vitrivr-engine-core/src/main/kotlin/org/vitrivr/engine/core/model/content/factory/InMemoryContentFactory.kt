package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryAudioContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryMeshContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.mesh.Model3D
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

class InMemoryContentFactory : ContentFactory {

    override fun newImageContent(bufferedImage: BufferedImage): ImageContent = InMemoryImageContent(bufferedImage)

    override fun newAudioContent(channel: Int, samplingRate: Int, audio: ShortBuffer): AudioContent = InMemoryAudioContent(channel, samplingRate, audio)

    override fun newTextContent(text: String): TextContent = InMemoryTextContent(text)

    override fun newMeshContent(model3D: Model3D): Model3DContent = InMemoryMeshContent(model3D)
}