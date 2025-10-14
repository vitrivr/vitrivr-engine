package org.vitrivr.engine.module.features.feature.external.implementations.ocr

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.features.fulltext.FulltextRetriever
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
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
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Implementation of the [OpticalCharacterRecognition] [ExternalAnalyser], which derives text descriptors
 * from an [ImageContent] by sending the image to the external OCR API (/extract-text).
 *
 * @author You
 * @version 1.0.0
 */
class OpticalCharacterRecognition : ExternalAnalyser<ContentElement<*>, TextDescriptor>() {

    companion object {
        /**
         * Requests OCR text for the given [ImageContent].
         *
         * @param content The [ImageContent] for which to request OCR.
         * @param hostname The hostname of the external OCR service.
         * @return The extracted [TextDescriptor].
         */
        fun analyse(content: ContentElement<*>, hostname: String): TextDescriptor {
            when (content) {
                is ImageContent -> URLEncoder.encode(content.toDataUrl(), StandardCharsets.UTF_8.toString())
                else -> throw IllegalArgumentException("Content '$content' not supported")
            }
            val encodedImage = URLEncoder.encode(content.toDataUrl(), StandardCharsets.UTF_8.toString())
            val url = "$hostname/extract/ocr"

            val response = httpRequest<TextDescriptor>(url, "data=$encodedImage")
                ?: throw IllegalArgumentException("Failed to generate OCR descriptor.")

            return response
        }
    }

    override val contentClasses = setOf(ImageContent::class)
    override val descriptorClass = TextDescriptor::class

    /**
     * Generates a prototypical [TextDescriptor] for this [OpticalCharacterRecognition].
     */
    override fun prototype(field: Schema.Field<*, *>) =
        TextDescriptor(UUID.randomUUID(), UUID.randomUUID(), Value.Text(""))

    /**
     * Generates and returns a new [Extractor] instance for this [OpticalCharacterRecognition].
     */
    override fun newExtractor(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        input: Operator<out Retrievable>,
        context: Context
    ): OpticalCharacterRecognitionExtractor {
        val host: String = field.parameters[HOST_PARAMETER_NAME] ?: HOST_PARAMETER_DEFAULT
        return OpticalCharacterRecognitionExtractor(input, this, field, host)
    }

    /**
     * Generates and returns a new [Extractor] instance for this [OpticalCharacterRecognition].
     */
    override fun newExtractor(
        name: String,
        input: Operator<out Retrievable>,
        context: Context
    ): OpticalCharacterRecognitionExtractor {
        val host: String = context.getProperty(name, HOST_PARAMETER_NAME) ?: HOST_PARAMETER_DEFAULT
        return OpticalCharacterRecognitionExtractor(input, this, name, host)
    }

    /**
     * Creates a retriever for OCR results, using fulltext search.
     */
     override fun newRetrieverForQuery(
        field: Schema.Field<ContentElement<*>, TextDescriptor>,
        query: Query,
        context: Context
    ): Retriever<ContentElement<*>, TextDescriptor> {
        //TODO("Not yet implemented")
        require(query is SimpleFulltextQuery) {
            "The query is not a SimpleFulltextQuery<Value.Text>."
        }
        return FulltextRetriever(field, query, context)
    }
}
