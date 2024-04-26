package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.retrievable.Ingested
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
     * @param context The [IndexContext] to use.
     */
    fun newTransformer(name: String, input: Operator<Ingested>, context: IndexContext): Transformer
}
