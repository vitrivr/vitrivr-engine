package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext

/**
 * A factory object for a specific [Transformer] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface TransformerFactory {
    /**
     * Creates a new [Transformer] instance from this [DecoderFactory].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Decoder].
     * @param context The [IndexContext] to use.
     */
    fun newOperator(name: String, input: Decoder, context: IndexContext): Transformer

    /**
     * Creates a new [Transformer] instance from this [DecoderFactory].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Transformer].
     * @param context The [IndexContext] to use.
     */
    fun newOperator(name: String, input: Transformer, context: IndexContext): Transformer
}
