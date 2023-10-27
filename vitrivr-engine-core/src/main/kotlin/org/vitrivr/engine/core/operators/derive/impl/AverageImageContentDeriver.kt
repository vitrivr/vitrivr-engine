package org.vitrivr.engine.core.operators.derive.impl

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.impl.DerivedImageContent
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.operators.derive.ContentDeriver
import org.vitrivr.engine.core.operators.derive.DerivateName
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.core.util.extension.setRGBArray
import java.awt.image.BufferedImage

class AverageImageContentDeriver : ContentDeriver<DerivedImageContent?> {

    companion object {
        val derivateName: DerivateName = "AverageImage"
    }

    override val derivateName: DerivateName = AverageImageContentDeriver.derivateName

    override fun derive(retrievable: Ingested, contentFactory: ContentFactory): DerivedImageContent? {

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

        return DerivedImageContent(contentFactory.newImageContent(averageImage), derivateName)
    }


}