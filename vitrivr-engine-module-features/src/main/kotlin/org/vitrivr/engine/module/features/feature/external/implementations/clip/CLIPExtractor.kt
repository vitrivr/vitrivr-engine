package org.vitrivr.engine.module.features.feature.external.implementations.clip

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.module.features.feature.external.BatchedImageEmbeddingExtractor

/**
 * Extracts CLIP image embeddings, optionally batching images across retrievables.
 *
 * @author Rahel Arnold
 */
class CLIPExtractor(
    input: Operator<out Retrievable>,
    analyser: CLIP,
    field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>?,
    name: String,
    host: String,
    batchSize: Int
) : BatchedImageEmbeddingExtractor(input, analyser, field, name, host, "clip", batchSize) {
    constructor(
        input: Operator<out Retrievable>,
        analyser: CLIP,
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        host: String
    ) : this(input, analyser, field, field.fieldName, host, 1)

    constructor(
        input: Operator<out Retrievable>, analyser: CLIP, name: String, host: String
    ) : this(input, analyser, null, name, host, 1)

    override fun embed(image: ImageContent, host: String) = CLIP.analyse(image, host)
}
