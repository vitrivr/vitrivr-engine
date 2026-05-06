package org.vitrivr.engine.index.segment.signal

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.module.features.feature.external.implementations.clip.CLIP

/**
 * In-memory CLIP adapter for segmentation.
 *
 * This class computes a CLIP vector for a decoded video window without creating
 * or persisting a FloatVectorDescriptor. Therefore, it can safely be used before
 * PersistRetrievableTransformer.
 *
 * @author Rahel Arnold
 *
 */
class VitrivrClipEmbeddingAdapter(
    private val context: Context
) : EmbeddingProvider {
    private val host: String =
        context["segmenter", "clipHost"]
            ?: context["clip", "host"]
            ?: "http://localhost:8888"

    override fun embed(retrievable: Retrievable): DoubleArray {
        val image = retrievable.content
            .filterIsInstance<ImageContent>()
            .firstOrNull()
            ?: throw IllegalArgumentException(
                "Cannot compute CLIP segmentation signal: retrievable contains no ImageContent. " +
                        "Available content types: ${retrievable.content.map { it.type }}"
            )

        val descriptor = CLIP.analyse(image, host)

        return descriptor.vector.value.map { it.toDouble() }.toDoubleArray()
    }

    fun canEmbed(retrievable: Retrievable): Boolean {
        return retrievable.content.any {
            it.type == ContentType.BITMAP_IMAGE || it is ImageContent
        }
    }
}