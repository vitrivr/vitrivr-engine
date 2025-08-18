package org.vitrivr.engine.index.aggregators.image

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.color.RGBColorContainer
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.decorators.TemporalContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.core.util.extension.setRGBArray
import org.vitrivr.engine.index.aggregators.AbstractAggregator
import java.awt.image.BufferedImage

/**
 * A [Transformer] that merges all [ImageContent] found in an [Ingested] into an average [ImageContent].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */
class AverageImageContentAggregator : OperatorFactory {
    /**
     * Creates a new [Instance] instance from this [AverageImageContentAggregator].
     *
     * @param name the name of the [AverageImageContentAggregator.Instance]
     * @param inputs Map of named input [Operator]s
     * @param context The [Context] to use.
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<out Retrievable> {
        require(inputs.size == 1)  { "The ${this::class.simpleName} only supports one input operator. If you want to combine multiple inputs, use explicit merge strategies." }
        return Instance(name, inputs.values.first(), context)
    }

    /**
     * The [Instance] returns by the [AverageImageContentAggregator]
     */
    private class Instance(name: String, input: Operator<out Retrievable>, context: Context) : AbstractAggregator(name, input, context) {
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
            val colors = List(firstImage.width * firstImage.height) { floatArrayOf(0f, 0f, 0f) }
            images.forEach { imageContent ->
                require(imageContent.height == height && imageContent.width == width) { "Unable to aggregate images! All images must have same dimension." }
                imageContent.content.getRGBArray().forEachIndexed { index, color ->
                    val rgb = RGBColorContainer(color)
                    colors[index][0] += rgb.red
                    colors[index][1] += rgb.green
                    colors[index][2] += rgb.blue
                }
            }

            val div = images.size.toFloat()
            val intColors = colors.map { c ->
                c[0] /= div
                c[1] /= div
                c[2] /= div
                RGBColorContainer(c).toRGBInt()
            }.toIntArray()

            /* Prepare buffered image. */
            val averageImage = BufferedImage(firstImage.width, firstImage.height, BufferedImage.TYPE_INT_RGB)
            averageImage.setRGBArray(intColors)

            /* Prepare content element. */
            val imageContent = this.context.contentFactory.newImageContent(averageImage)
            val ret = if (images.any { it is SourcedContent.Temporal }) {
                object : ImageContent by imageContent, TemporalContent.TimeSpan {
                    override val startNs: Long =
                        images.filterIsInstance<SourcedContent.Temporal>().minOf { it.timepointNs }
                    override val endNs: Long =
                        images.filterIsInstance<SourcedContent.Temporal>().maxOf { it.timepointNs }
                }
            } else {
                imageContent
            }

            /* Return content. */
            return listOf(ret)
        }
    }
}
