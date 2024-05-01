package org.vitrivr.engine.index.aggregators

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * A [Transformer] that selects the first [ContentElement] of each type in an [Ingested] and drops all the others.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class FirstContentAggregator : TransformerFactory {

    /**
     * Returns an [FirstContentAggregator.Instance].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator]
     * @param context The [IndexContext] to use.
     * @return [FirstContentAggregator.Instance]
     */
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer = Instance(input, context)

    /**
     * The [Instance] returned by the [FirstContentAggregator]
     */
    private class Instance(override val input: Operator<out Retrievable>, context: Context) : AbstractAggregator(input, context) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> = content.groupBy { it.type }.mapNotNull { (_, elements) -> elements.firstOrNull() }
    }
}
