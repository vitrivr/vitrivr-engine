package org.vitrivr.engine.base.resolvers

import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.operators.ingest.Resolvable
import org.vitrivr.engine.core.operators.ingest.Resolver
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageOnDiskResolver(val format: ImageResolverFormat = ImageResolverFormat.JPG, val location: String = "./thumbnails") : Resolver {
    override fun resolve(id: RetrievableId): Resolvable {
        val thumbnailFile = File("$location/${id}.${format.value}")
        return ResolvableImage(ImageIO.read(thumbnailFile))
    }

    override fun saveBufferedImage(id: RetrievableId, img: BufferedImage) {
        val thumbnailFile = File("$location/${id}.${format.value}")
        thumbnailFile.mkdirs()
        javax.imageio.ImageIO.write(img, format.value, thumbnailFile)
    }

    override fun saveAny(id: RetrievableId, any: Any) {
        throw IllegalArgumentException("ImageOnDiskResolver is designed specifically for image resolution. Received an unsupported type: ${any::class.java.name}. Please check your configuration.")
    }
}