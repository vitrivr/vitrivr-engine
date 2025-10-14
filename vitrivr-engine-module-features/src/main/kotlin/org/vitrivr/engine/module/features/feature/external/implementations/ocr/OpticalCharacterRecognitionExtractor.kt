package org.vitrivr.engine.module.features.feature.external.implementations.ocr

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.ContentType
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * [OpticalCharacterRecognitionExtractor] implementation of an [AbstractExtractor] for [OpticalCharacterRecognition].
 *
 * This extractor sends image content to the external OCR service (our Flask/APIFlask endpoint)
 * and creates a [TextDescriptor] containing the extracted text.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class OpticalCharacterRecognitionExtractor : AbstractExtractor<ContentElement<*>, TextDescriptor> {

    private val host: String

    constructor(input: Operator<out Retrievable>, analyser: OpticalCharacterRecognition, field: Schema.Field<ContentElement<*>, TextDescriptor>, host: String) : super(input, analyser, field) {
        this.host = host
    }
    constructor(input: Operator<out Retrievable>, analyser: OpticalCharacterRecognition, name: String, host: String) : super(input, analyser, name) {
        this.host = host
    }
    /**
     * Determines if this extractor can process the given [Retrievable].
     */
    override fun matches(retrievable: Retrievable): Boolean =
        retrievable.content.any { it.type == ContentType.BITMAP_IMAGE }

    /**
     * Performs OCR extraction on all [ImageContent] elements within a [Retrievable].
     */
    override fun extract(retrievable: Retrievable) =
        retrievable.content
            .filterIsInstance<ImageContent>()
            .map { imageContent ->
                // The OCR.analyse function should send the image (base64) to /extract-text
                OpticalCharacterRecognition.analyse(imageContent, this.host)
                    .copy(retrievableId = retrievable.id, field = this@OpticalCharacterRecognitionExtractor.field)
            }
}
