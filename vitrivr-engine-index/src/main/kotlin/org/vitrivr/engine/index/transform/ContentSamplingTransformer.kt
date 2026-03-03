package org.vitrivr.engine.index.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer
import kotlin.text.toIntOrNull

/**
 * A [Transformer] that samples the input [Flow] and only passes through every n-th element.
 *
 * @author Ralph Gasser
 * @version 1.2.0
 */
class ContentSamplingTransformer : OperatorFactory {
    /**
     * Creates a new [Instance] instance from this [ContentSamplingTransformer].
     *
     * @param name the name of the [ContentSamplingTransformer.Instance]
     * @param inputs Map of named input [Operator]s
     * @param context The [Context] to use.
     */
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<out Retrievable> {
        require(inputs.size == 1)  { "The ${this::class.simpleName} only supports one input operator. If you want to combine multiple inputs, use explicit merge strategies." }
        return Instance(name, inputs.values.first(),  context[name, "sample"]?.toIntOrNull() ?: 10)
    }

    private class Instance(override val name: String, override val input: Operator<out Retrievable>, private val sample: Int) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
            var counter = 0L
            return this.input.toFlow(scope).filter {
                (counter++) % this@Instance.sample == 0L
            }
        }
    }
}
