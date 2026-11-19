package org.vitrivr.engine.module.features.feature.external.implementations.siglip2

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.module.features.feature.external.BatchedImageEmbeddingExtractor

/**
 * Extracts SigLip2 image embeddings, optionally batching images across retrievables.
 *
 * @author Rahel Arnold
 */
class SigLip2Extractor(
    input: Operator<out Retrievable>,
    analyser: SigLip2,
    field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>?,
    name: String,
    host: String,
    batchSize: Int
) : BatchedImageEmbeddingExtractor(input, analyser, field, name, host, "siglip2", batchSize) {
    constructor(
        input: Operator<out Retrievable>,
        analyser: SigLip2,
        field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>,
        host: String
    ) : this(input, analyser, field, field.fieldName, host, 1)

    constructor(
        input: Operator<out Retrievable>, analyser: SigLip2, name: String, host: String
    ) : this(input, analyser, null, name, host, 1)

    override fun embed(image: ImageContent, host: String) = SigLip2.analyse(image, host)
}
