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
class OCRExtractor : FesExtractor<ImageContent, TextDescriptor> {

    constructor(
        input: Operator<Retrievable>,
        field: Schema.Field<ImageContent, TextDescriptor>,
        analyser: ExternalFesAnalyser<ImageContent, TextDescriptor>,
        parameters: Map<String, String>
    ) : super(input, field, analyser, parameters)

    constructor(
        input: Operator<Retrievable>,
        name: String,
        analyser: ExternalFesAnalyser<ImageContent, TextDescriptor>,
        parameters: Map<String, String>
    ) : super(input, name, analyser, parameters)

    /** The [OcrApi] used to perform extraction with. */
    private val api = OcrApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries)


    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param batch The [Retrievable]s to process.
     * @return List of resulting [Descriptor]s grouped by [Retrievable].
     */
    override fun extract(batch: List<Retrievable>): List<List<TextDescriptor>> {
        val flatResults = this.api.analyseBatched(batch.flatMap { it.content.filterIsInstance<ImageContent>() }).map { result ->
            TextDescriptor(UUID.randomUUID(), null, result, this.field)
        }

        var index = 0
        return batch.map { retrievable ->
            retrievable.content.filterIsInstance<ImageContent>().map { filtered ->
                flatResults[index++].let { TextDescriptor(it.id, retrievable.id, it.value, it.field) }
            }
        }
    }
}
