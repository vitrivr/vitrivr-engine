package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*


/**
 * Implementation of the [ImageClassification] [ExternalFesAnalyser] that uses the [ApiWrapper] to classify images.
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class ImageClassification : ExternalFesAnalyser<ImageContent, LabelDescriptor>() {
    companion object{
        const val CLASSES_PARAMETER_NAME = "classes"
        const val THRESHOLD_PARAMETER_NAME = "threshold"
        const val TOPK_PARAMETER_NAME = "top_k"
    }

    override val model = "clip-vit-large-patch14"

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = LabelDescriptor::class

    /**
     * Generates a prototypical [LabelDescriptor] for this [ImageClassification].
     *
     * @param field [Schema.Field] to create the prototype for.
     * @return [LabelDescriptor]
     */
    override fun prototype(field: Schema.Field<*,*>): LabelDescriptor {
        return LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), mapOf("label" to Value.String(""), "confidence" to Value.Float(0.0f)))
    }

    override fun newRetrieverForContent(field: Schema.Field<ImageContent, LabelDescriptor>, content: Collection<ImageContent>, context: QueryContext): Retriever<ImageContent, LabelDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, LabelDescriptor>, query: Query, context: QueryContext): Retriever<ImageContent, LabelDescriptor> {
        TODO("Not yet implemented")
    }

    /**
     * Performs analysis on the provided [Retrievable] using the given [ApiWrapper].
     *
     * @param retrievables [Retrievable] to analyse.
     * @param api [ApiWrapper] to use for analysis.
     * @param field The [Schema.Field] to perform the analysis for.
     * @param parameters Additional parameters for the analysis.
     */
    @Suppress("UNCHECKED_CAST")
    override fun analyse(retrievables: Retrievable, api: ApiWrapper, field: Schema.Field<ImageContent, LabelDescriptor>?, parameters: Map<String, String>): List<LabelDescriptor> {
        val classes = parameters[CLASSES_PARAMETER_NAME]?.split(",") ?: throw IllegalArgumentException("No classes provided")
        val topk = parameters[TOPK_PARAMETER_NAME]?.toInt() ?: 1
        val threshold = parameters[THRESHOLD_PARAMETER_NAME]?.toFloat() ?: 0.0f
        val content = retrievables.findContent { it is ImageContent } as List<ImageContent>
        if (content.isEmpty()) return emptyList()

        /* Perform classification and perform top K results. */
        val results = api.zeroShotImageClassification(content.map { it.content }, classes)
        return results
            .mapIndexed { index, score -> LabelDescriptor(UUID.randomUUID(), retrievables.id, mapOf("label" to Value.String(classes[index]), "confidence" to Value.Float(score[index])), field) }
            .filter { it.confidence.value >= threshold }
            .sortedByDescending { it.confidence.value }
            .take(topk)
    }
}