package org.vitrivr.engine.core.operators.aggregate.impl

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.operators.aggregate.ContentAggregator


/**
 * Derives the 'most representative' image out of a list of images as defined by the smallest pixel-wise distance
 * to the pixel-wise average.
 */
class MostRepresentativeContentAggregator : ContentAggregator {

    override fun aggregate(content: List<ContentElement<*>>, contentFactory: ContentFactory): List<ContentElement<*>> {
        val images = content.filterIsInstance<ImageContent>()
        if (images.isEmpty()) {
            return emptyList()
        }
        TODO()
    }
}