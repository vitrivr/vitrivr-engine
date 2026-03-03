package org.vitrivr.engine.module.features.feature.external.implementations.asr

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import org.vitrivr.engine.module.features.feature.external.implementations.clip.CLIP
import java.net.URLEncoder
import java.util.*

/**
 * Implementation of the [AutomaticSpeechRecognition] [ExternalAnalyser],
 * which derives text descriptors from an [AudioContent] by sending the audio
 * to the external ASR API (/extract/asr).
 *
 * @author You
 * @version 1.0.0
 */
class AutomaticSpeechRecognition : ExternalAnalyser<ContentElement<*>, TextDescriptor>() {

    companion object {
        /**
         * Requests ASR text for the given [AudioContent].
         *
         * @param content  The [AudioContent] for which to request transcription.
         * @param hostname The hostname of the external ASR service.
         * @return The extracted [TextDescriptor].
         */
        fun analyse(content: ContentElement<*>, hostname: String): TextDescriptor {

            val audioDataUrl = when (content) {
                is AudioContent -> content.toDataURL()
                else -> throw IllegalArgumentException("Content '$content' not supported")
            }

            val url = "$hostname/extract/asr"
            //println("Sending data to $url prefix: ${audioDataUrl.take(40)}...")

            val encoded = URLEncoder.encode(audioDataUrl, Charsets.UTF_8.name())
            val response = httpRequest<TextDescriptor>(url, requestBody = "data=$encoded")
                ?: throw IllegalArgumentException("Failed to generate ASR descriptor.")
            return response
        }

    }

    override val contentClasses = setOf(AudioContent::class)
    override val descriptorClass = TextDescriptor::class

    /** Creates a prototypical [TextDescriptor]. */
    override fun prototype(field: Schema.Field<*, *>) =
        TextDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Text(""))

    /** Creates a new [Extractor] for ASR. */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        input: Operator<out Retrievable>,
        context: Context
    ): Extractor<ContentElement<*>, TextDescriptor> {
        val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return AutomaticSpeechRecognitionExtractor(input, this, field, host)
    }

    /** Creates a new [Extractor] for ASR. */
    override fun newExtractor(
        name: String,
        input: Operator<out Retrievable>,
        context: Context
    ): Extractor<ContentElement<*>, TextDescriptor> {
        val host: String = context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT
        return AutomaticSpeechRecognitionExtractor(input, this, name, host)
    }

    /** Creates a retriever for ASR results using full-text search. */
    override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        query: Query,
        context: Context
    ): Retriever<ContentElement<*>, TextDescriptor> {
        require(query is SimpleFulltextQuery) {
            "The query is not a SimpleFulltextQuery<Value.Text>."
        }
        return FulltextRetriever(field, query, context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [ASR].*
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param content An array of [ContentElement] elements to use with the [Retriever]
     * @param context The [QueryContext] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [CLIP]
     * @throws [UnsupportedOperationException], if this [CLIP] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForContent(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        content: Map<String, ContentElement<*>>,
        context: Context
    ): Retriever<ContentElement<*>, TextDescriptor> {
        val host = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT

        val descriptors = content.map { (_, element) ->
            var res = when (element) {
                is AudioContent -> AutomaticSpeechRecognition.analyse(element, host)
                is TextContent -> element.content as TextDescriptor
                else -> throw IllegalArgumentException("Content '$element' not supported")
            }
            res
        }
        /* Return retriever. */
        return this.newRetrieverForDescriptors(field, descriptors, context)
    }

    /**
     * Generates and returns a new [Retriever] instance for this [ASR].
     *
     * @param field The [Schema.Field] to create an [Retriever] for.
     * @param descriptors An array of [TextDescriptor] elements to use with the [Retriever]
     * @param context The [Context] to use with the [Retriever]
     *
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    override fun newRetrieverForDescriptors(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        descriptors: Collection<TextDescriptor>,
        context: Context
    ): Retriever<ContentElement<*>, TextDescriptor> {
        /* Prepare query parameters. */
        val k = context.getProperty(field.fieldName, "limit")?.toLongOrNull() ?: 1000L
        val fetchVector = context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false

        /* Return retriever. */
        return this.newRetrieverForQuery(
            field,
            SimpleFulltextQuery(
                descriptors.first().value,
                limit = k
            ),
            context
        )
    }

}
