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
     * @param input The input [Decoder].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    fun newOperator(input: Decoder, context: IndexContext, parameters: Map<String, Any> = emptyMap()): Transformer

    /**
     * Creates a new [Transformer] instance from this [DecoderFactory].
     *
     * @param input The input [Transformer].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    fun newOperator(input: Transformer, context: IndexContext, parameters: Map<String, Any> = emptyMap()): Transformer
}