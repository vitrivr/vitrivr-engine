package org.vitrivr.engine.base.features.external.implementations.classification

import org.vitrivr.engine.base.features.external.api.ZeroShotClassificationApi
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.implementations.classification.ImageClassification.Companion.CLASSES_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.implementations.classification.ImageClassification.Companion.THRESHOLD_PARAMETER_NAME
import org.vitrivr.engine.base.features.external.implementations.classification.ImageClassification.Companion.TOPK_PARAMETER_NAME
import org.vitrivr.engine.core.model.content.element.ImageContent
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
class ImageClassificationExtractor : FesExtractor<ImageContent, LabelDescriptor> {

    constructor(
        input: Operator<Retrievable>,
        field: Schema.Field<ImageContent, LabelDescriptor>,
        analyser: ExternalFesAnalyser<ImageContent, LabelDescriptor>,
        parameters: Map<String, String>
    ) : super(input, field, analyser, parameters)

    constructor(
        input: Operator<Retrievable>,
        name: String,
        analyser: ExternalFesAnalyser<ImageContent, LabelDescriptor>,
        parameters: Map<String, String>
    ) : super(input, name, analyser, parameters)

    /** The [ZeroShotClassificationApi] used to perform extraction with. */
    private val api by lazy {
        ZeroShotClassificationApi(
            this.host,
            this.model,
            this.timeoutMs,
            this.pollingIntervalMs,
            this.retries
        )
    }


    override fun extract(retrievables: List<Retrievable>): List<List<LabelDescriptor>> {
        val classes = this.parameters[CLASSES_PARAMETER_NAME]?.split(",")
            ?: throw IllegalArgumentException("No classes provided.")

        val topK = this.parameters[TOPK_PARAMETER_NAME]?.toInt() ?: 1
        val threshold = this.parameters[THRESHOLD_PARAMETER_NAME]?.toFloat() ?: 0.0f

        val content = retrievables.mapIndexed { idx, retrievable ->
            retrievable.content.filterIsInstance<ImageContent>().map { idx to (it to classes) }
        }.flatten()

        return this.api.analyseBatched(content.map{it.second}).zip(content.map{it.first}).map { (result, idx) ->
            result.mapIndexed { idy, confidence ->
                LabelDescriptor(
                    UUID.randomUUID(),
                    retrievables[idx].id,
                    mapOf(
                        "label" to Value.String(classes[idy]),
                        "confidence" to Value.Float(confidence.value.toFloat())
                    ),
                    this.field
                )
            }.filter { it.confidence.value >= threshold }.sortedByDescending { it.confidence.value }.take(topK)
        }
    }
}