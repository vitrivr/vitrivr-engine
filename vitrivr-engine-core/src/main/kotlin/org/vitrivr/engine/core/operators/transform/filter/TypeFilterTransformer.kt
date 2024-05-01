package org.vitrivr.engine.core.operators.transform.filter

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * A [Transformer] that filters [Ingested] objects based on their [Ingested.type].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TypeFilterTransformer : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        val predicate = context[name, "type"] ?: throw IllegalArgumentException("The type filter transformer requires a type name.")
        return Instance(input, predicate)
    }

    private class Instance(input: Operator<out Retrievable>, val type: String) : AbstractFilterTransformer(input, { it.type == type })
}
