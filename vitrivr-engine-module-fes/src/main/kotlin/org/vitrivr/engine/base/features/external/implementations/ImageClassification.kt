package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.struct.LabelDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*

class ImageClassification : ExternalFesAnalyser<ImageContent, LabelDescriptor>() {
    companion object{
        const val CLASSES_PARAMETER_NAME = "classes"
        const val THRESHOLD_PARAMETER_NAME = "threshold"
        const val TOPK_PARAMETER_NAME = "top_k"
    }
    override val defaultModel = "clip-vit-large-patch14"
    override fun analyseFlattened(content: List<ImageContent>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<LabelDescriptor>> {

        val classes = parameters[CLASSES_PARAMETER_NAME]?.split(",") ?: throw IllegalArgumentException("No classes provided")
        val topk = parameters[TOPK_PARAMETER_NAME]?.toInt() ?: 1
        val threshold = parameters[THRESHOLD_PARAMETER_NAME]?.toFloat() ?: 0.0f

        val results = apiWrapper.zeroShotImageClassification(content.map { it.content }, classes)

        val descriptors = mutableListOf<List<LabelDescriptor>>()

        for (result in results) {
            val filteredResults = result
                .mapIndexed { index, score -> LabelDescriptor(UUID.randomUUID(), null, Value.String(classes[index]), Value.Float(score), score >= threshold) }
                .filter { it.confidence.value >= threshold }
                .sortedByDescending { it.confidence.value }
                .take(topk)

            descriptors.add(filteredResults)
        }

        return descriptors
    }


    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = LabelDescriptor::class

    override fun prototype(field: Schema.Field<*,*>): LabelDescriptor {
        return LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""), Value.Float(0.0f), true)
    }

    override fun newRetrieverForContent(field: Schema.Field<ImageContent, LabelDescriptor>, content: Collection<ImageContent>, context: QueryContext): Retriever<ImageContent, LabelDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForQuery(field: Schema.Field<ImageContent, LabelDescriptor>, query: Query, context: QueryContext): Retriever<ImageContent, LabelDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ImageContent, LabelDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, String>): Extractor<ImageContent, LabelDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        val batchSize = parameters[BATCHSIZE_PARAMETER_NAME]?.toIntOrNull() ?: BATCHSIZE_PARAMETER_DEFAULT.toInt()
        return object : FesExtractor<LabelDescriptor, ImageContent, ImageClassification>(input, field, persisting, batchSize){
            override fun assignRetrievableId(descriptor: LabelDescriptor, retrievableId: RetrievableId): LabelDescriptor {
                return descriptor.copy(retrievableId = retrievableId)
            }
        }
    }
}