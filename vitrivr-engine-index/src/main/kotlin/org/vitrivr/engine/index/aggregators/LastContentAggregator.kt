package org.vitrivr.engine.index.aggregators

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * A [Transformer] that selects the last [ContentElement] of each type in an [Ingested] and drops all the others.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class LastContentAggregator : TransformerFactory {

    /**
     * Returns an [LastContentAggregator.Instance].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator].
     * @param context The [IndexContext] to use.
     * @return [LastContentAggregator.Instance]
     */
    override fun newTransformer(name: String, input: Operator<Retrievable>, context: IndexContext): Transformer = Instance(input, context)

    /**
     * The [Instance] returns by the [LastContentAggregator]
     */
    private class Instance(override val input: Operator<Retrievable>, context: IndexContext) : AbstractAggregator(input, context) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> = content.groupBy { it.type }.mapNotNull { (_, elements) -> elements.lastOrNull() }
    }
}
