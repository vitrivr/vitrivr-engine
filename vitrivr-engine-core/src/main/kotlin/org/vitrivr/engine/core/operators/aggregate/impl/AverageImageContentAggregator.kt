package org.vitrivr.engine.core.operators.aggregate.impl

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.decorators.TemporalContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.operators.aggregate.ContentAggregator
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.core.util.extension.setRGBArray
import java.awt.image.BufferedImage

/**
 * A [ContentAggregator] that returns an average image of all [ImageContent].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class AverageImageContentAggregator : ContentAggregator {

    override fun aggregate(content: List<ContentElement<*>>, contentFactory: ContentFactory): List<ContentElement<*>> {
        /* Filter out images. */
        val images = content.filterIsInstance<ImageContent>()
        if (images.isEmpty()) {
            return emptyList()
        }

        /* Compute average image. */
        val firstImage = images.first()
        val height = firstImage.height
        val width = firstImage.width
        val colors = List(firstImage.width * firstImage.height) { MutableRGBFloatColorContainer() }
        images.forEach { imageContent ->
            require(imageContent.height == height && imageContent.width == width) { "Unable to aggregate images! All images must have same dimension." }
            imageContent.content.getRGBArray().forEachIndexed { index, color ->
                colors[index] += RGBByteColorContainer.fromRGB(color)
            }
        }

        val div = images.size.toFloat()
        val intColors = colors.map {
            (it / div).toByteContainer().toRGBInt()
        }.toIntArray()

        /* Prepare buffered image. */
        val averageImage = BufferedImage(firstImage.width, firstImage.height, BufferedImage.TYPE_INT_RGB)
        averageImage.setRGBArray(intColors)

        /* Prepare content element. */
        val imageContent = contentFactory.newImageContent(averageImage)
        val ret = if (images.any { it is SourcedContent.Temporal }) {
            object : ImageContent by imageContent, TemporalContent.TimeSpan {
                override val startNs: Long = images.filterIsInstance<SourcedContent.Temporal>().minOf { it.timepointNs }
                override val endNs: Long = images.filterIsInstance<SourcedContent.Temporal>().maxOf { it.timepointNs }
            }
        } else {
            imageContent
        }

        /* Return content. */
        return listOf(ret)
    }
}