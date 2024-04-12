package org.vitrivr.engine.base.features.external.implementations

import org.vitrivr.engine.base.features.external.common.ApiWrapper
import org.vitrivr.engine.base.features.external.common.ExternalFesAnalyser
import org.vitrivr.engine.base.features.external.common.FesExtractor
import org.vitrivr.engine.base.features.external.common.FulltextRetriever
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.StringDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.types.Value.String as StringValue
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import java.util.*


class ASR : ExternalFesAnalyser<ContentElement<*>, StringDescriptor>() {

    override val defaultModel = "whisper"
    override fun analyse(content: List<ContentElement<*>>, apiWrapper: ApiWrapper, parameters: Map<String, String>): List<List<StringDescriptor>> {
        val audioContents = content.filterIsInstance<AudioContent>()
        if (audioContents.isEmpty()) {
            throw IllegalArgumentException("No audio content found in the provided content.")
        }
        val result = apiWrapper.automatedSpeechRecognition(audioContents)
        return result.map { listOf(StringDescriptor(UUID.randomUUID(), null, StringValue(it))) }
    }

    override val contentClasses = setOf(AudioContent::class)
    override val descriptorClass = StringDescriptor::class

    override fun prototype(field: Schema.Field<*, *>): StringDescriptor {
        return StringDescriptor(UUID.randomUUID(), UUID.randomUUID(), StringValue(""))
    }

    override fun newExtractor(field: Schema.Field<ContentElement<*>, StringDescriptor>, input: Operator<Retrievable>, context: IndexContext, persisting: Boolean, parameters: Map<String, Any>): Extractor<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        return object : FesExtractor<StringDescriptor, ASR>(input, field, persisting) {
            override fun assignRetrievableId(descriptor: StringDescriptor, retrievableId: RetrievableId): StringDescriptor {
                return descriptor.copy(retrievableId = retrievableId)
            }
        }
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param query The [Query] to use with the [Retriever].
     * @param context The [QueryContext] to use with the [Retriever].
     *
     * @return A new [FulltextRetriever] instance for this [ASR]
     */
    override fun newRetrieverForQuery(field: Schema.Field<ContentElement<*>, StringDescriptor>, query: Query, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }
        require(query is SimpleFulltextQuery) { "The query is not a fulltext query. This is a programmer's error!" }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [FulltextRetriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [Content] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     * @return [FulltextRetriever]
     */
    override fun newRetrieverForContent(field: Schema.Field<ContentElement<*>, StringDescriptor>, content: Collection<ContentElement<*>>, context: QueryContext): Retriever<ContentElement<*>, StringDescriptor> {
        require(field.analyser == this) { "The field '${field.fieldName}' analyser does not correspond with this analyser. This is a programmer's error!" }

        /* Prepare query parameters. */
        val text = content.filterIsInstance<TextContent>().firstOrNull() ?: throw IllegalArgumentException("No text content found in the provided content.")
        val limit = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L

        return this.newRetrieverForQuery(field, SimpleFulltextQuery(value = Value.String(text.content), limit = limit), context)

    }

}