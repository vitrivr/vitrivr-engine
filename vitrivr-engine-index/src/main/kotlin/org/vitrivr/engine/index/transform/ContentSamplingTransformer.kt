package org.vitrivr.engine.index.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * A [Transformer] that samples the input [Flow] and only passes through every n-th element.
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ContentSamplingTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, parameters: Map<String, String>, context: Context): Transformer = Instance(input, name, parameters["sample"]?.toIntOrNull() ?: 10)

    private class Instance(
        override val input: Operator<out Retrievable>,
        override val name: String,
        private val sample: Int
    ) : Transformer {
        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
            var counter = 0L
            return this.input.toFlow(scope).filter {
                (counter++) % this@Instance.sample == 0L
            }
        }
    }
}
