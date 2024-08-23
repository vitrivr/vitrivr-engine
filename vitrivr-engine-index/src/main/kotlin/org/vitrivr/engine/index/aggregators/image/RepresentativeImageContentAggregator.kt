package org.vitrivr.engine.index.aggregators.image

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.color.RGBColorContainer
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.index.aggregators.content.AbstractAggregator

/**
 * A [Transformer] that derives the 'most representative' [ImageContent] from all [ImageContent] found in an [Ingested].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */
class RepresentativeImageContentAggregator : TransformerFactory {

    /**
     * Returns an [RepresentativeImageContentAggregator.Instance].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     * @return [RepresentativeImageContentAggregator.Instance]
     */
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer = Instance(input, context as IndexContext, name)


    /**
     * The [Instance] returns by the [RepresentativeImageContentAggregator]
     */
    private class Instance(override val input: Operator<out Retrievable>, override val context: IndexContext, name: String
    ) : AbstractAggregator(input, context, name) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> {
            val images = content.filterIsInstance<ImageContent>()
            if (images.isEmpty()) {
                return emptyList()
            }

            if (images.size == 1) {
                return images
            }

            /* Compute average image. */
            val firstImage = images.first()
            val height = firstImage.height
            val width = firstImage.width
            val colors = List(firstImage.width * firstImage.height) { floatArrayOf(0.0f, 0.0f, 0.0f) }
            images.forEach { imageContent ->
                require(imageContent.height == height && imageContent.width == width) { "Unable to aggregate images! All images must have same dimension." }
                imageContent.content.getRGBArray().forEachIndexed { index, color ->
                    val rgb = RGBColorContainer(color)
                    colors[index][0] += rgb.red
                    colors[index][1] += rgb.green
                    colors[index][2] += rgb.blue
                }
            }

            /* normalize */
            val div = images.size.toFloat()
            for (color in colors) {
                color[0] /= div
                color[1] /= div
                color[2] /= div
            }

            /* find image with smallest pixel-wise distance */
            val mostRepresentative = images.minBy { imageContent ->
                imageContent.content.getRGBArray().mapIndexed { index, color ->
                    RGBColorContainer(color).distanceTo(RGBColorContainer(colors[index]))
                }.sum()

            }
            return listOf(mostRepresentative)
        }
    }
}
