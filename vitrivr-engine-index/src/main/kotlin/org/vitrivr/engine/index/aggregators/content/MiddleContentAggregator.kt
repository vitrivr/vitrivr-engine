package org.vitrivr.engine.index.aggregators.content

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.index.aggregators.AbstractAggregator

/**
 * A [Transformer] that selects the middle [ContentElement] of each type in an [Ingested] and drops all the others.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class MiddleContentAggregator : OperatorFactory {
    /**
     * Creates a new [Instance] instance from this [LastContentAggregator].
     *
     * @param name the name of the [LastContentAggregator.Instance]
     * @param inputs Map of named input [Operator]s
     * @param context The [Context] to use.
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<out Retrievable> {
        require(inputs.size == 1)  { "The ${this::class.simpleName} only supports one input operator. If you want to combine multiple inputs, use explicit merge strategies." }
        return Instance(name, inputs.values.first(), context)
    }

    /**
     * The [Instance] returns by the [MiddleContentAggregator]
     */
    private class Instance(name: String, input: Operator<out Retrievable>, context: Context) : AbstractAggregator(name, input, context) {
        override fun aggregate(content: List<ContentElement<*>>) = content.groupBy { it.type }.mapNotNull { (_, elements) ->
            if (elements.isNotEmpty()) {
                elements[Math.floorDiv(elements.size, 2)]
            } else {
                null
            }
        }
    }
}
