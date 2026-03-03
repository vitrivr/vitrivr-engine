package org.vitrivr.engine.core.operators.transform.filter

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Transformer

/**
 * A [Transformer] that filters [Ingested] objects based on their [Ingested.type].
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class TypeFilterTransformer : OperatorFactory {
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Transformer {
        val predicate = context[name, "type"] ?: throw IllegalArgumentException("The type filter transformer requires a type name.")
        return Instance(name, inputs.values.first(), predicate)
    }

    private class Instance(name: String, input: Operator<out Retrievable>, val type: String) : AbstractFilterTransformer(name, input, { it.type == type })
}
