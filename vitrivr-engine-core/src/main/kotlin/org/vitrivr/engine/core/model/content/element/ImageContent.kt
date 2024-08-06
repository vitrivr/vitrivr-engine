package org.vitrivr.engine.core.model.content.element

import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.source.file.MimeType
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*
import javax.imageio.ImageIO

/**
 * A visual (image) [ContentElement].
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.1.0
 */
interface ImageContent: ContentElement<BufferedImage> {
    /** Width of the [BufferedImage] held by this [ContentElement]. */
    val width: Int
        get() = this.content.width

    /** Height of the [BufferedImage] held by this [ContentElement]. */
    val height: Int
        get() = this.content.height

    /** The [ContentType] of an [ImageContent] is always [ContentType.BITMAP_IMAGE]. */
    override val type: ContentType
        get() = ContentType.BITMAP_IMAGE

    /**
     * Converts this [AudioContent] to a [ByteBuffer] containing the content in the WAVE format.
     *
     * @return [ByteBuffer] containing the audio in the WAVE format.
     */
    fun toBytes(mimeType: MimeType = MimeType.PNG): ByteBuffer {
        require(mimeType.mediaType == MediaType.IMAGE) { "MimeType needs to be an image type" }
        val out = ByteArrayOutputStream()
        ImageIO.write(this.content, mimeType.fileExtension, out)
        return ByteBuffer.wrap(out.toByteArray())
    }

    /**
     * Converts the [ImageContent] to a data URL encoding a [MimeType] file.
     *
     * @return Data URL
     */
    fun toDataUrl(mimeType: MimeType = MimeType.PNG): String {
        val buffer = this.toBytes(mimeType)
        val base64 = Base64.getEncoder().encodeToString(buffer.array())
        return "data:${mimeType.mimeType};base64,$base64"
    }
}