package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.Model3DContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryAudioContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryMeshContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.mesh.Model3D
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
    override fun newContentFactory(schema: Schema, context: Context): ContentFactory = Instance()
    private class Instance() : ContentFactory {
        override fun newImageContent(bufferedImage: BufferedImage) = InMemoryImageContent(bufferedImage)
        override fun newAudioContent(channels: Short, sampleRate: Int, audio: ShortBuffer) = InMemoryAudioContent(channels, sampleRate, audio)
        override fun newTextContent(text: String): TextContent = InMemoryTextContent(text)
        override fun newMeshContent(model3D: Model3D): Model3DContent = InMemoryMeshContent(model3D)
    }
}
