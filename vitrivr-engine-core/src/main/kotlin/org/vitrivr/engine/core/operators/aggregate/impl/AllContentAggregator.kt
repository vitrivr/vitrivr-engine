package org.vitrivr.engine.core.operators.aggregate.impl

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.aggregate.ContentAggregator

/**
 * A simple [ContentAggregator] that does not aggregate and instead just maps input content to output content.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class AllContentAggregator : ContentAggregator {
    override fun aggregate(content: List<ContentElement<*>>, contentFactory: ContentFactory): List<ContentElement<*>> = content
}