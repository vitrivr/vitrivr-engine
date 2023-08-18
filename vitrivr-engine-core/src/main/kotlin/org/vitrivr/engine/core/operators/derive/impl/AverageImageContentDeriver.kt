package org.vitrivr.engine.core.operators.derive.impl

import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.content.ImageContent
import org.vitrivr.engine.core.model.content.derived.DerivedImageContent
import org.vitrivr.engine.core.model.content.derived.impl.InMemoryDerivedImageContent
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.derive.ContentDeriver
import org.vitrivr.engine.core.operators.derive.ContentDerivers
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.awt.image.BufferedImage

class AverageImageContentDeriver : ContentDeriver<DerivedImageContent?> {

    companion object {
        val derivateName: DerivateName = "AverageImage"
    }

    override val derivateName: DerivateName = AverageImageContentDeriver.derivateName

    init {
        ContentDerivers.register(this)
    }

    override fun derive(retrievable: IngestedRetrievable): DerivedImageContent? {

        val images = retrievable.content.filterIsInstance<ImageContent>()

        if (images.isEmpty()) {
            return null
        }

        val firstImage = images.first()

        val matchingImages =
            images.filter { it.source == firstImage.source && it.image.width == firstImage.image.width && it.image.height == firstImage.image.height }

        val colors = List(firstImage.image.width * firstImage.image.height) { MutableRGBFloatColorContainer() }

        matchingImages.forEach { imageContent ->
            imageContent.image.getRGB(
                0,
                0,
                imageContent.image.width,
                imageContent.image.height,
                null,
                0,
                imageContent.image.width
            ).forEachIndexed { index, color ->
                colors[index] += RGBByteColorContainer.fromRGB(color)
            }
        }

        val div = matchingImages.size.toFloat()

        val intColors = colors.map {
            (it / div).toByteContainer().toRGBInt()
        }.toIntArray()

        val averageImage = BufferedImage(firstImage.image.width, firstImage.image.height, BufferedImage.TYPE_INT_RGB)
        averageImage.setRGB(
            0, 0, averageImage.width, averageImage.height, intColors, 0, averageImage.width
        )

        return InMemoryDerivedImageContent(source = firstImage.source, image = averageImage, name = derivateName)
    }


}