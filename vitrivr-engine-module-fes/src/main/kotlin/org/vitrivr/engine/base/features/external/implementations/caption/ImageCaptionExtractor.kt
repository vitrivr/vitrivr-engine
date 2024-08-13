package org.vitrivr.engine.base.features.external.implementations.caption

import org.vitrivr.engine.base.features.external.api.ConditionalImageCaptioningApi
import org.vitrivr.engine.base.features.external.api.ImageCaptioningApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.implementations.caption.ImageCaption.Companion.PROMPT_PARAMETER_NAME
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class ImageCaptionExtractor : FesExtractor<ImageContent, TextDescriptor> {

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

    /** The [ImageCaptioningApi] used to perform extraction with. */
    private val captioningApi by lazy { ImageCaptioningApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries) }

    /** The [ConditionalImageCaptioningApi] used to perform extraction with. */
    private val conditionalCaptioningApi by lazy { ConditionalImageCaptioningApi(this.host, this.model, this.timeoutMs, this.pollingIntervalMs, this.retries) }

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<TextDescriptor> {
        val prompt = this.field?.parameters?.get(PROMPT_PARAMETER_NAME)?.let { InMemoryTextContent(it) }
        return retrievable.content.mapNotNull {
            if (it is ImageContent) {
                val result = if (prompt == null) {
                    this.captioningApi.analyse(it)
                } else {
                    this.conditionalCaptioningApi.analyse(it to prompt)
                }
                if (result != null) {
                    TextDescriptor(UUID.randomUUID(), retrievable.id, result, this.field)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}