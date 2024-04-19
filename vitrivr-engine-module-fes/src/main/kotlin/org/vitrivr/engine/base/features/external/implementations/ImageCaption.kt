package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.common.FulltextRetriever
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import java.util.*

class ImageCaption : ExternalFesAnalyser<ContentElement<*>, StringDescriptor>() {
    companion object {
        const val PROMPT_PARAMETER_NAME = "prompt"
    }

    override val defaultModel = "blip2"
    override fun analyse(content: List<ContentElement<*>>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<StringDescriptor>> {
        val prompt = parameters[PROMPT_PARAMETER_NAME]
        val imageContents = content.filterIsInstance<ImageContent>()
        if (imageContents.isEmpty()) {
            throw IllegalArgumentException("No image content found in the provided content.")
        }
        if (prompt != null) {
            val result = apiWrapper.conditionalImageCaptioning(imageContents.map { it.content }, List(imageContents.size) { prompt })
            return result.map { listOf(StringDescriptor(UUID.randomUUID(), null, Value.String(it))) }
        }
        val result = apiWrapper.imageCaptioning(imageContents.map { it.content })
        return result.map { listOf(StringDescriptor(UUID.randomUUID(), null, Value.String(it))) }
    }

    override val contentClasses = setOf(ContentElement::class)
    override val descriptorClass = StringDescriptor::class
    override fun prototype(field: Schema.Field<*, *>): StringDescriptor = StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.String(""), true)

    override fun newExtractor(field: Schema.Field<ContentElement<*>, StringDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return object : FesExtractor<StringDescriptor, ImageCaption>(input, field, persisting) {
            override fun assignRetrievableId(descriptor: StringDescriptor, retrievableId: RetrievableId): StringDescriptor {
                return descriptor.copy(retrievableId = retrievableId)
            }
        }
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ImageCaption].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ImageCaption]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, StringDescriptor>, query: Query, context: QueryContext): FulltextRetriever {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ImageCaption].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, StringDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): FulltextRetriever {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L

        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = Value.String(text.content), limit = limit), context)
    }


}