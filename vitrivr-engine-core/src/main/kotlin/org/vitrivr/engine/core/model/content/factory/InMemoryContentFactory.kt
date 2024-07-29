package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.*
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryAudioContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryMesh3DContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
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

    override fun newContentFactory(schema: Schema, context: Context): ContentFactory = Instance()

    private class Instance() : ContentFactory {

        override fun newImageContent(bufferedImage: BufferedImage): ImageContent = InMemoryImageContent(bufferedImage)

        override fun newAudioContent(channels: Short, samplingRate: Int, audio: ShortBuffer): AudioContent =
            InMemoryAudioContent(channels, samplingRate, audio)

        override fun newTextContent(text: String): TextContent = InMemoryTextContent(text)

        override fun newMeshContent(model3d: Model3d): Model3DContent = InMemoryMesh3DContent(model3d)
    }
}
