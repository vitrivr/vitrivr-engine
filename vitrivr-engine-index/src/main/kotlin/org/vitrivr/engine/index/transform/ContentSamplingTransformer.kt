package org.vitrivr.engine.index.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.decorators.SourcedContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.TransformerFactory
import org.vitrivr.engine.core.source.Source
import kotlin.reflect.KClass

/**
 * A [Transformer] that samples the input [Flow] and only passes through every n-th element.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ContentSamplingTransformer : TransformerFactory {
    override fun newOperator(input: Decoder, context: IndexContext, parameters: Map<String, String>): Transformer = Instance(input, parameters["sample"]?.toIntOrNull() ?: 10)
    override fun newOperator(input: Transformer, context: IndexContext, parameters: Map<String, String>): Transformer = Instance(input, parameters["sample"]?.toIntOrNull() ?: 10)

    private class Instance(override val input: Operator<ContentElement<*>>, private val sample: Int) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<ContentElement<*>> {
            val sources = mutableMapOf<KClass<out ContentElement<*>>, Source>()
            val counters = mutableMapOf<KClass<out ContentElement<*>>, Int>()
            return this.input.toFlow(scope).filter { value: ContentElement<*> ->
                if (value is SourcedContent) { /* Only source content can be sampled. */
                    val clazz = value::class
                    if (sources[clazz] == null || sources[clazz] != value.source) {
                        sources[clazz] = value.source
                        counters[clazz] = 0
                    }
                    val pass = (counters[clazz]!! % this.sample == 0)
                    counters[clazz] = counters[clazz]!! + 1
                    pass
                } else {
                    true
                }
            }
        }
    }
}