package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
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

class ImageClass : ExternalFesAnalyser<ContentElement<*>, LabelDescriptor>() {
    companion object{
        const val CLASSES_PARAMETER_NAME = "classes"
    }
    override val defaultModel = "clip-vit-large-patch14"
    override fun analyse(content: List<ContentElement<*>>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<LabelDescriptor>> {
        val classes = parameters[CLASSES_PARAMETER_NAME]?.split(",") ?: throw IllegalArgumentException("No classes provided")
        val imageContents = content.filterIsInstance<ImageContent>()
        if (imageContents.isEmpty()) {
            throw IllegalArgumentException("No image content found in the provided content.")
        }
        val results = apiWrapper.zeroShotImageClassification(imageContents.map { it.content }, classes)
        val descriptors = mutableListOf<List<LabelDescriptor>>()
        for (result in results) {

            descriptors.add(result.mapIndexed { index, classResult -> LabelDescriptor(UUID.randomUUID(), null, Value.String(classes[index]), Value.Float(classResult), true) })
        }
        return descriptors
    }

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = LabelDescriptor::class

    override fun prototype(field: Schema.Field<*,*>): LabelDescriptor {
        return LabelDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""), Value.Float(0.0f), true)
    }

    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, LabelDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, LabelDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, LabelDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, LabelDescriptor> {
        TODO("Not yet implemented")
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, LabelDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, LabelDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return object : FesExtractor<LabelDescriptor, ImageClass>(input, field, persisting){
            override fun assignRetrievableId(descriptor: LabelDescriptor, retrievableId: RetrievableId): LabelDescriptor {
                return descriptor.copy(retrievableId = retrievableId)
            }
        }
    }
}