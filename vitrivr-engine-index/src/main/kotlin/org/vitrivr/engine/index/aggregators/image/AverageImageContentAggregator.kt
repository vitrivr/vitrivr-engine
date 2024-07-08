package org.vitrivr.engine.index.aggregators.image

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.decorators.TemporalContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.core.util.extension.setRGBArray
import org.vitrivr.engine.index.aggregators.AbstractAggregator
import java.awt.image.BufferedImage

/**
 * A [Transformer] that merges all [ImageContent] found in an [Ingested] into an average [ImageContent].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
class AverageImageContentAggregator : TransformerFactory {

    /**
     * Returns an [AverageImageContentAggregator.Instance].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator] .
     * @param context The [IndexContext] to use.
     * @return [AverageImageContentAggregator.Instance]
     */
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer = Instance(input, context as IndexContext, name)

    /**
     * The [Instance] returns by the [AverageImageContentAggregator]
     */
    private class Instance(override val input: Operator<out Retrievable>, override val context: IndexContext, name: String) : AbstractAggregator(input, context, name) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> {
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
            val imageContent = this.context.contentFactory.newImageContent(averageImage)
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
}
