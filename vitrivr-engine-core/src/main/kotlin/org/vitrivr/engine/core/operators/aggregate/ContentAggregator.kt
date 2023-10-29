package org.vitrivr.engine.core.operators.aggregate

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested

/**
 * A [ContentAggregator] is used to aggregate the content contained in an [Ingested] according to some defined logic.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
interface ContentAggregator {

    /**
     * The name of the [ContentAggregator].
     */
    fun aggregate(content: List<ContentElement<*>>, contentFactory: ContentFactory): List<ContentElement<*>>
}