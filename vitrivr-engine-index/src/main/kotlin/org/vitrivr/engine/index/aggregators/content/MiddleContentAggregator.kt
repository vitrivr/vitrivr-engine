package org.vitrivr.engine.index.aggregators.content

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.index.aggregators.AbstractAggregator

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
     * @param context The [Context] to use.
     * @return [MiddleContentAggregator.Instance]
     */
    override fun newTransformer(
        name: String,
        input: Operator<out Retrievable>,
        parameters: Map<String, String>,
        context: Context
    ): Transformer = Instance(input, parameters, context, name)

    /**
     * The [Instance] returns by the [MiddleContentAggregator]
     */
    private class Instance(
        override val input: Operator<out Retrievable>,
        parameters: Map<String, String>,
        context: Context,
        name: String
    ) : AbstractAggregator(input, parameters, context, name) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> =
            content.groupBy { it.type }.mapNotNull { (_, elements) ->
                if (elements.isNotEmpty()) {
                    elements[Math.floorDiv(elements.size, 2)]
                } else {
                    null
                }
            }
    }
}
