package org.vitrivr.engine.core.operators.derive.impl

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.color.RGBByteColorContainer
import org.vitrivr.engine.core.model.content.decorators.DerivedContent
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.impl.DerivedImageContent
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.operators.derive.ContentDeriver
import org.vitrivr.engine.core.operators.derive.DerivateName
import org.vitrivr.engine.core.util.extension.getRGBArray


/**
 * Derives the 'most representative' image out of a list of images as defined by the smallest pixel-wise distance
 * to the pixel-wise average.
 */
class MostRepresentativeFrameDeriver : ContentDeriver<DerivedImageContent?> {

    companion object {
        val derivateName: DerivateName = "MostRepresentativeFrame"
    }

    override val derivateName: DerivateName = MostRepresentativeFrameDeriver.derivateName

    override fun derive(retrievable: Ingested, contentFactory: ContentFactory): DerivedImageContent? {

        require(retrievable is RetrievableWithContent) { "Can only derive content from a retrievable that has content." }

        val averageImage =
            (retrievable.deriveContent(AverageImageContentDeriver.derivateName, contentFactory) as? DerivedImageContent)
                ?: return null

        val images = retrievable.content.filterIsInstance<ImageContent>().filter { it !is DerivedContent }

        if (images.isEmpty()) {
            return null
        }

        val averageImageColors =
            averageImage.getContent().getRGBArray().map { RGBByteColorContainer.fromRGB(it).toFloatContainer() }

        val closestToAverage = images.map { img ->
            img to img.getContent().getRGBArray().asSequence().mapIndexed { index, c ->
                RGBByteColorContainer.fromRGB(c).toFloatContainer().distanceTo(averageImageColors[index])
            }.sum()
        }.minBy { it.second }.first

        return DerivedImageContent(closestToAverage, derivateName)
    }
}