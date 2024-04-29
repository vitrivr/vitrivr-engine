package org.vitrivr.engine.index.aggregators

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Aggregator
import org.vitrivr.engine.core.operators.ingest.AggregatorFactory
import org.vitrivr.engine.core.operators.ingest.Segmenter

/**
 * A [Aggregator] that returns the first [ContentElement] of each type.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FirstContentAggregator : AggregatorFactory {

    /**
     * Returns an [FirstContentAggregator.Instance].
     *
     * @param name The name of the [Aggregator]
     * @param input The [Segmenter] to use as input.
     * @param context The [IndexContext] to use.
     * @return [AllContentAggregator.Instance]
     */
    override fun newOperator(name: String, input: Segmenter, context: IndexContext): Aggregator = Instance(input, context)

    /**
     * The [Instance] returns by the [AggregatorFactory]
     */
    private class Instance(override val input: Operator<Retrievable>, context: IndexContext) : AbstractAggregator(input, context) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> = content.groupBy { it.type }.mapNotNull { (_, elements) -> elements.firstOrNull() }
    }
}
