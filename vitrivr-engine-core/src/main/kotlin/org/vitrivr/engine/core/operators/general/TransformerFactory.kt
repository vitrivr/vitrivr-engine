package org.vitrivr.engine.core.operators.general

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A factory object for a specific [Transformer] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface TransformerFactory {
    /**
     * Creates a new [Transformer] instance from this [TransformerFactory].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator].
     * @param schema The [Schema] to be used.
     * @param properties the properties of the transformer.
     */
    fun newTransformer(name: String, input: Operator<out Retrievable>, schema: Schema, properties: Map<String, String>): Transformer
}
