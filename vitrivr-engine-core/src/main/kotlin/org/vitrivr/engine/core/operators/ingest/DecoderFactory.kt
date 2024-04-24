package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.context.IndexContext

/**
 * A factory object for a specific [Decoder] type.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
interface DecoderFactory {
    /**
     * Creates a new [Decoder] instance from this [DecoderFactory].
     *
     * @param name The name of the [Decoder]
     * @param input The input [Enumerator].
     * @param context The [IndexContext] to use.
     */
    fun newOperator(name: String, input: Enumerator, context: IndexContext): Decoder
}
