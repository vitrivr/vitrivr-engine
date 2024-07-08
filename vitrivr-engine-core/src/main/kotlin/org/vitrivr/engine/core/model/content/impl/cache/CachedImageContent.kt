package org.vitrivr.engine.core.model.content.impl.cache

import org.vitrivr.engine.core.model.content.element.ImageContent
import java.awt.image.BufferedImage
import java.lang.ref.SoftReference
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import javax.imageio.ImageIO

/**
 * A [ImageContent] implementation that is backed by a cache file.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class CachedImageContent(override val path: Path, image: BufferedImage) : ImageContent, CachedContent {

    override val id: UUID = UUID.randomUUID()

    /** The [SoftReference] of the [BufferedImage] used for caching. */
    private var reference: SoftReference<BufferedImage> = SoftReference(image)

    /** The width of the [BufferedImage] (is stored explicitly). */
    override val width = image.width

    /** The height of the [BufferedImage] (is stored explicitly). */
    override val height = image.height

    /** The [BufferedImage] contained in this [CachedImageContent]. */
    override val content: BufferedImage
        get() {
            var image = this.reference.get()
            if (image == null) {
                image = reload()
                this.reference = SoftReference(image)
            }
            return image
        }

    init {
        Files.newOutputStream(this.path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).use {
            ImageIO.write(image, "png", it)
        }
    }

    /**
     * Reloads the buffered image from disk.
     *
     * @return [BufferedImage]
     */
    private fun reload(): BufferedImage = Files.newInputStream(this.path, StandardOpenOption.READ).use {
        ImageIO.read(it)
    }
}