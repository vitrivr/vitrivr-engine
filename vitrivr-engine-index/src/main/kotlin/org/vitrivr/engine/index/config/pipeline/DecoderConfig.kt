package org.vitrivr.engine.index.config.pipeline

import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.Decoder

/**
 * Configuration element for a [Decoder] stage in an operator pipeline.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
@Serializable
data class DecoderConfig(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
    val nextTransformer: TransformerConfig? = null,
    val nextSegmenter: SegmenterConfig? = null
) {
    init {
        if (this.nextTransformer != null) {
            require(this.nextSegmenter == null) { "A decoder can either be followed by a transformer OR a segmenter." }
        } else {
            require(this.nextSegmenter != null) { "A decoder can either be followed by a transformer OR a segmenter." }
        }
    }
}