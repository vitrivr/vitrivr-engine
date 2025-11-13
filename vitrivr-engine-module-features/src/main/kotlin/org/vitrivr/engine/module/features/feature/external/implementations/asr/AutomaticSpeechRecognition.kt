package org.vitrivr.engine.module.features.feature.external.implementations.asr

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.Query
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
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
}
