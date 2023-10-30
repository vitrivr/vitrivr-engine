package org.vitrivr.engine.index.aggregators

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Aggregator
import org.vitrivr.engine.core.operators.ingest.AggregatorFactory
import org.vitrivr.engine.core.operators.ingest.Segmenter

/**
 * A simple [Aggregator] that does not aggregate and instead just maps input content to output content.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class AllContentAggregator : AggregatorFactory {
    /**
     * Returns an [AllContentAggregator.Instance].
     *
     * @param input The [Segmenter] to use as input.
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     * @return [AllContentAggregator.Instance]
     */
    override fun newOperator(input: Segmenter, context: IndexContext, parameters: Map<String, Any>): Aggregator = Instance(input, context)

    /**
     * The [Instance] simply copies the incoming [Ingested] and replaces the content with the aggregated content.
     */
    private class Instance(override val input: Operator<Retrievable>, context: IndexContext) : AbstractAggregator(input, context) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> = content
    }
}

