package org.vitrivr.engine.index.aggregators

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * A [Transformer] that selects the middle [ContentElement] of each type in an [Ingested] and drops all the others.
 *
 * @author
 * @version 1.0.0
 */
class MiddleContentAggregator : TransformerFactory {

    /**
     * Returns an [MiddleContentAggregator.Instance].
     *
     * @param name The name of the [Aggregator]
     * @param input The [Segmenter] to use as input.
     * @param context The [IndexContext] to use.
     * @return [MiddleContentAggregator.Instance]
     */
    override fun newTransformer(name: String, input: Operator<Retrievable>, context: IndexContext): Transformer = Instance(input, context)

    /**
     * The [Instance] returns by the [MiddleContentAggregator]
     */
    private class Instance(override val input: Operator<Retrievable>, context: IndexContext) : AbstractAggregator(input, context) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> = content.groupBy { it.type }.mapNotNull { (_, elements) ->
            if (elements.isNotEmpty()) {
                elements[Math.floorDiv(elements.size, 2)]
            } else {
                null
            }
        }
    }
}
