package org.vitrivr.engine.index.aggregators

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Aggregator
import org.vitrivr.engine.core.operators.ingest.AggregatorFactory
import org.vitrivr.engine.core.operators.ingest.Segmenter
import org.vitrivr.engine.core.source.Source
import java.nio.ShortBuffer

/**
 * A simple [Aggregator] that aggregates audio content.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class AudioContentAggregator : AggregatorFactory {
    /**
     * Returns an [AudioContentAggregator.Instance].
     *
     * @param input The [Segmenter] to use as input.
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     * @return [AudioContentAggregator.Instance]
     */
    override fun newOperator(input: Segmenter, context: IndexContext, parameters: Map<String, String>): Aggregator = Instance(input, context)

    /**
     * The [Instance] filters all the audio
     */
    private class Instance(override val input: Operator<Retrievable>, context: IndexContext) : AbstractAggregator(input, context) {
        override fun aggregate(content: List<ContentElement<*>>): List<ContentElement<*>> {
            val audioContent: List<AudioContent> = content.filter { it is AudioContent}.map { it as AudioContent }
            val totalCapacity = audioContent.sumOf { it.content.remaining() }

            val combinedBuffer = ShortBuffer.allocate(totalCapacity)

            audioContent.forEach { combinedBuffer.put(it.content) }
            combinedBuffer.flip()  // Prepare the buffer for reading


            return listOf(object : AudioContent, SourcedContent.Temporal {
                override val source: Source = (content.first() as SourcedContent.Temporal).source
                override val timepointNs: Long = (content.first() as SourcedContent.Temporal).timepointNs
                override val channels: Short = audioContent.first().channels
                override val samplingRate: Int = audioContent.first().samplingRate
                override val content: ShortBuffer = combinedBuffer

            })
        }
    }
}

