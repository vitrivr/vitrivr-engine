package org.vitrivr.engine.base.features.external.implementations.classification

import org.vitrivr.engine.base.features.external.api.ZeroShotClassificationApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.implementations.classification.ImageClassification.Companion.CLASSES_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.implementations.classification.ImageClassification.Companion.THRESHOLD_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.implementations.classification.ImageClassification.Companion.TOPK_PARAMETER_NAME
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class ImageClassificationExtractor(
    input: Operator<Retrievable>,
    field: Schema.Field<ImageContent, LabelDescriptor>?,
    analyser: ExternalFesAnalyser<ImageContent, LabelDescriptor>,
    model: String,
    parameters: Map<String, String>
) : FesExtractor<ImageContent, LabelDescriptor>(input, field, analyser, model, parameters) {


    /** The [ZeroShotClassificationApi] used to perform extraction with. */
    private val api by lazy { ZeroShotClassificationApi(host, model, timeoutMs, pollingIntervalMs, retries) }

    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable): List<LabelDescriptor> {
        val classes = this.parameters[CLASSES_PARAMETER_NAME]?.split(",") ?: throw IllegalArgumentException("No classes provided.")
        val topK = this.parameters[TOPK_PARAMETER_NAME]?.toInt() ?: 1
        val threshold = this.parameters[THRESHOLD_PARAMETER_NAME]?.toFloat() ?: 0.0f
        return retrievable.content.flatMap { content ->
            if (content is ImageContent) {
                val result = this.api.analyse(content to classes)
                result?.mapIndexed { index, score ->
                    LabelDescriptor(UUID.randomUUID(), retrievable.id, mapOf("label" to Value.String(classes[index]), "confidence" to score), this.field)
                }?.filter { it.confidence.value >= threshold }?.sortedByDescending { it.confidence.value }?.take(topK)
                    ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
}