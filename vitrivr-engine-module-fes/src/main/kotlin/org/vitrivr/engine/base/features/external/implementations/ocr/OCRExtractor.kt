package org.vitrivr.engine.base.features.external.implementations.ocr

import org.vitrivr.engine.base.features.external.api.OcrApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * A [FesExtractor] to perform [OCR] on [ImageContent].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class OCRExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ImageContent, TextDescriptor>?,
    analyser: ExternalFesAnalyser<ImageContent, TextDescriptor>,
    parameters: Map<String, String>
) : FesExtractor<ImageContent, TextDescriptor>(input, field, analyser, parameters) {
    /** The [OcrApi] used to perform extraction with. */
    private val api = OcrApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<TextDescriptor> {
        val content = this.filterContent(retrievable)
        return content.mapNotNull { audio ->
            val result = this.api.analyse(audio)
            if (result != null) {
                TextDescriptor(UUID.randomUUID(), retrievable.id, result, this.field)
            } else {
                null
            }
        }
    }
}
