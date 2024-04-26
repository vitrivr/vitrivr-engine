package org.vitrivr.engine.index.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.TransformerFactory
import org.vitrivr.engine.core.source.Source

/**
 * A [Transformer] that samples the input [Flow] and only passes through every n-th element.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ContentSamplingTransformer : TransformerFactory {
    override fun newOperator(name: String, input: Decoder, context: IndexContext, ): Transformer = Instance(input, context[name,"sample"]?.toIntOrNull() ?: 10)
    override fun newOperator(name: String, input: Transformer, context: IndexContext): Transformer = Instance(input, context[name,"sample"]?.toIntOrNull() ?: 10)

    private class Instance(override val input: Operator<ContentElement<*>>, private val sample: Int) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<ContentElement<*>> {
            val sources = mutableMapOf<ContentType, Source>()
            val counters = mutableMapOf<ContentType, Int>()
            return this.input.toFlow(scope).filter { value: ContentElement<*> ->
                if (value is SourcedContent) { /* Only source content can be sampled. */
                    val type = value.type
                    if (sources[type] == null || sources[type] != value.source) {
                        sources[type] = value.source
                        counters[type] = 0
                    }
                    val pass = (counters[type]!! % this.sample == 0)
                    counters[type] = counters[type]!! + 1
                    pass
                } else {
                    true
                }
            }
        }
    }
}
