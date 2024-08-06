package org.vitrivr.engine.base.features.external.implementations.ocr

import org.vitrivr.engine.base.features.external.api.OcrApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
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
    field: Schema.Field<ImageContent, StringDescriptor>?,
    analyser: ExternalFesAnalyser<ImageContent, StringDescriptor>,
    model: String,
    parameters: Map<String, String>
) : FesExtractor<ImageContent, StringDescriptor>(input, field, analyser, model, parameters) {
    /** The [OcrApi] used to perform extraction with. */
    private val api = OcrApi(host, model, timeoutMs, pollingIntervalMs, retries)

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<StringDescriptor> {
        val content = retrievable.content.filterIsInstance<ImageContent>()
        return content.mapNotNull { audio ->
            val result = this.api.analyse(audio)
            if (result != null) {
                StringDescriptor(UUID.randomUUID(), retrievable.id, result)
            } else {
                null
            }
        }
    }
}
