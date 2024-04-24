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
     * @param name The name of the [Segmenter]
     * @param input The input [Transformer].
     * @param context The [IndexContext] to use.
     */
    fun newOperator(name: String, input: Transformer, context: IndexContext): Segmenter

    /**
     * Creates a new [Segmenter] instance from this [SegmenterFactory].
     *
     * @param name The name of the [Segmenter]
     * @param input The input [Segmenter].
     * @param context The [IndexContext] to use.
     */
    fun newOperator(name: String, input: Decoder, context: IndexContext): Segmenter
}
