package org.vitrivr.engine.index.segment.signal

import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * Interface for embedding providers used during adaptive segmentation.
 *
 * Implementations generate in-memory embeddings for retrievables without persisting descriptors.
 *
 * @author Rahel Arnold
 */
interface EmbeddingProvider {
    fun embed(retrievable: Retrievable): DoubleArray
}