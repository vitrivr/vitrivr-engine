package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.model.content.element.*
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
 * @version 1.0.0
 */
class InMemoryContentFactory : ContentFactoriesFactory {

    override fun newContentFactory(schema: Schema, parameters: Map<String, String>): ContentFactory = Instance()

    private class Instance() : ContentFactory {

        override fun newImageContent(bufferedImage: BufferedImage): ImageContent = InMemoryImageContent(bufferedImage)

        override fun newAudioContent(channels: Short, samplingRate: Int, audio: ShortBuffer): AudioContent =
            InMemoryAudioContent(channels, samplingRate, audio)

        override fun newTextContent(text: String): TextContent = InMemoryTextContent(text)

        override fun newMeshContent(model3D: Model3D): Model3DContent = InMemoryMeshContent(model3D)
    }
}