package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.DescriptorContent
import org.vitrivr.engine.core.model.content.element.Model3dContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryAudioContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryModel3dContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import org.vitrivr.engine.core.model.metamodel.Schema
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

/**
 * A [ContentFactory] that keeps [ContentElement]s in memory.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
class InMemoryContentFactory : ContentFactoriesFactory {
    override fun newContentFactory(schema: Schema, parameters: Map<String, String>): ContentFactory = Instance()
    private class Instance : ContentFactory {
        override fun newImageContent(bufferedImage: BufferedImage) = InMemoryImageContent(bufferedImage)
        override fun newAudioContent(channels: Short, sampleRate: Int, audio: ShortBuffer) =
            InMemoryAudioContent(channels, sampleRate, audio)

        override fun newTextContent(text: String): DescriptorContent<TextDescriptor> = InMemoryTextContent(text)
        override fun newMeshContent(model3d: Model3d): Model3dContent = InMemoryModel3dContent(model3d)
    }
}
