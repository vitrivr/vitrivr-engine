package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement

import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.Model3dContent
import org.vitrivr.engine.core.model.content.element.TextContent

import org.vitrivr.engine.core.model.mesh.texturemodel.Model3d
import java.awt.image.BufferedImage
import java.nio.ShortBuffer

/**
 * A [ContentFactory] generates [ContentElement]s for processing in vitrivr.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface ContentFactory {
    /**
     * Creates a new [ImageContent] object from the provided [BufferedImage].
     *
     * @param bufferedImage The image to be wrapped in an [ImageContent] object.
     * @return A new [ImageContent]  object containing the provided [BufferedImage].
     */
    fun newImageContent(bufferedImage: BufferedImage): ImageContent

    /**
     * Creates a new [AudioContent] object from the provided audio data.
     *
     * @param channels The number of audio channels.
     * @param sampleRate The sample rate of the audio data.
     * @param audio The audio data as a [ShortBuffer].
     * @return A new [AudioContent] object containing the provided audio data.
     */
    fun newAudioContent(channels: Short, sampleRate: Int, audio: ShortBuffer): AudioContent

    /**
     * Creates a new [TextContent] object from the provided text [String].
     *
     * @param text The text to be wrapped in a TextContent object.
     * @return A new [TextContent]  object containing the provided text.
     */
    fun newTextContent(text: String): TextContent

    /**
     * Creates a new [Model3dContent] object from the provided [Model3d] data.
     *
     * @param model3d The 3D model data to be wrapped in a [Model3dContent] object.
     * @return A new [Model3dContent] object containing the provided 3D model data.
     */
    fun newMeshContent(model3d: Model3d): Model3dContent
}