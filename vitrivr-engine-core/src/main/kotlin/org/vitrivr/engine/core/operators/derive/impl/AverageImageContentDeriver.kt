package org.vitrivr.engine.core.operators.derive.impl

import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.content.impl.InMemoryDerivedImageContent
import org.vitrivr.engine.core.model.content.impl.InMemoryImageContent
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithContent
import org.vitrivr.engine.core.operators.derive.ContentDeriver
import org.vitrivr.engine.core.operators.derive.ContentDerivers
import org.vitrivr.engine.core.operators.derive.DerivateName
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.core.util.extension.setRGBArray
import java.awt.image.BufferedImage

class AverageImageContentDeriver : ContentDeriver<InMemoryDerivedImageContent?> {

    companion object {
        val derivateName: DerivateName = "AverageImage"
    }

    override val derivateName: DerivateName = AverageImageContentDeriver.derivateName

    init {
        ContentDerivers.register(this)
    }

    override fun derive(retrievable: Ingested): InMemoryDerivedImageContent? {

        require(retrievable is RetrievableWithContent) { "Can only derive content from a retrievable that has content." }

        /* Filter out images. */
        val images = retrievable.content.filterIsInstance<ImageContent>()
        if (images.isEmpty()) {
            return null
        }

        /* Compute average image. */
        val firstImage = images.first()
        val matchingImages = images.filter { it.getContent().width == firstImage.getContent().width && it.getContent().height == firstImage.getContent().height }
        val colors = List(firstImage.getContent().width * firstImage.getContent().height) { MutableRGBFloatColorContainer() }
        matchingImages.forEach { imageContent ->
            imageContent.getContent().getRGBArray().forEachIndexed { index, color ->
                colors[index] += RGBByteColorContainer.fromRGB(color)
            }
        }

        val div = matchingImages.size.toFloat()
        val intColors = colors.map {
            (it / div).toByteContainer().toRGBInt()
        }.toIntArray()
        val averageImage = BufferedImage(firstImage.getContent().width, firstImage.getContent().height, BufferedImage.TYPE_INT_RGB)
        averageImage.setRGBArray(intColors)

        return InMemoryDerivedImageContent(image = averageImage, name = derivateName)
    }


}