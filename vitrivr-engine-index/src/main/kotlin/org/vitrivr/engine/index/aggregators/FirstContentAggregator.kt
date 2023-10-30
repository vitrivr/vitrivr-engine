package org.vitrivr.engine.index.aggregators

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
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
     * @param input The [Segmenter] to use as input.
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     * @return [AllContentAggregator.Instance]
     */
    override fun newOperator(input: Segmenter, context: IndexContext, parameters: Map<String, Any>): Aggregator = Instance(input, context)

    /**
     * The [Instance] returns by the [AggregatorFactory]
     */
    private class Instance(override val input: Operator<Retrievable>, context: IndexContext) : AbstractAggregator(input, context) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> {
            val firstImage = content.firstOrNull { it is ImageContent } as? ImageContent
            val firstAudio = content.firstOrNull { it is AudioContent } as? AudioContent
            val firstText = content.firstOrNull { it is TextContent } as? TextContent
            return listOfNotNull(firstImage, firstAudio, firstText)
        }
    }
}