package org.vitrivr.engine.core.operators.aggregate.impl

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.operators.aggregate.ContentAggregator

/**
 * A [ContentAggregator] that returns the first [ContentElement] of each type.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FirstContentAggregator : ContentAggregator {
    override fun aggregate(content: List<ContentElement<*>>, contentFactory: ContentFactory): List<ContentElement<*>> {
        val firstImage = content.firstOrNull { it is ImageContent } as? ImageContent
        val firstAudio = content.firstOrNull { it is AudioContent } as? AudioContent
        val firstText = content.firstOrNull { it is TextContent } as? TextContent
        return listOfNotNull(firstImage, firstAudio, firstText)
    }
}