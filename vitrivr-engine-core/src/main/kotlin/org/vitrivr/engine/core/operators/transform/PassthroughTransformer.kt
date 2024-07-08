package org.vitrivr.engine.core.operators.transform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

class PassthroughTransformer : TransformerFactory{
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        return Instance(input)
    }

    private class Instance(input: Operator<out Retrievable>) : Transformer {
        override val input: Operator<out Retrievable> = input

        override fun toFlow(scope: CoroutineScope): Flow<Retrievable> {
            return input.toFlow(scope)
        }
    }
}
