package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.DescriptorContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.Model3dContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

interface ContentFactory {
    fun newImageContent(bufferedImage: BufferedImage): ImageContent

    fun newAudioContent(channels: Short, sampleRate: Int, audio: ShortBuffer): AudioContent

    fun newTextContent(text: String): DescriptorContent<TextDescriptor>

    fun newMeshContent(model3d: Model3d): Model3dContent
}