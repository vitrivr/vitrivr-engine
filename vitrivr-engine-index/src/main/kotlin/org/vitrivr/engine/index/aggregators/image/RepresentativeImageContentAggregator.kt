package org.vitrivr.engine.index.aggregators.image

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.color.MutableRGBFloatColorContainer
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Aggregator
import org.vitrivr.engine.core.operators.ingest.AggregatorFactory
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.util.extension.getRGBArray
import org.vitrivr.engine.index.aggregators.AbstractAggregator

/**
 * A [Aggregator] thatderives the 'most representative' image out of a list of images as defined by the smallest pixel-wise distance.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
class RepresentativeImageContentAggregator : AggregatorFactory {

    /**
     * Returns an [RepresentativeImageContentAggregator.Instance].
     *
     * @param name The name of the [Aggregator]
     * @param input The [Segmenter] to use as input.
     * @param context The [IndexContext] to use.
     * @return [RepresentativeImageContentAggregator.Instance]
     */
    override fun newOperator(name: String, input: Segmenter, context: IndexContext): Aggregator =
        Instance(input, context)

    /**
     * The [Instance] returns by the [AggregatorFactory]
     */
    private class Instance(override val input: Operator<Retrievable>, context: IndexContext) :
        AbstractAggregator(input, context) {
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
            val colors = List(firstImage.width * firstImage.height) { MutableRGBFloatColorContainer() }
            images.forEach { imageContent ->
                require(imageContent.height == height && imageContent.width == width) { "Unable to aggregate images! All images must have same dimension." }
                imageContent.content.getRGBArray().forEachIndexed { index, color ->
                    colors[index] += RGBByteColorContainer.fromRGB(color)
                }
            }

            /* normalize */
            val div = images.size.toFloat()
            colors.forEach { it /= div }

            /* find image with smallest pixel-wise distance */
            val mostRepresentative = images.minBy { imageContent ->

                imageContent.content.getRGBArray().mapIndexed { index, color ->
                    RGBByteColorContainer.fromRGB(color).toFloatContainer().distanceTo(colors[index])
                }.sum()

            }

            return listOf(mostRepresentative)


        }
    }
}
