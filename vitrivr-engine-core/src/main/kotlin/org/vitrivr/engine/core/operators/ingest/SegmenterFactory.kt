package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext

/**
 * A factory object for a specific [Segmenter] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface SegmenterFactory {
    /**
     * Creates a new [Segmenter] instance from this [SegmenterFactory].
     *
     * @param input The input [Transformer].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    fun newOperator(input: Transformer, context: IndexContext, parameters: Map<String, String> = emptyMap()): Segmenter

    /**
     * Creates a new [Segmenter] instance from this [SegmenterFactory].
     *
     * @param input The input [Segmenter].
     * @param context The [IndexContext] to use.
     * @param parameters Optional set of parameters.
     */
    fun newOperator(input: Decoder, context: IndexContext, parameters: Map<String, String> = emptyMap()): Segmenter
}