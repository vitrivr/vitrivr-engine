package org.vitrivr.engine.base.features.external.implementations.CLIP_Text

import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.base.features.external.common.ExternalWithFloatVectorDescriptorAnalyser
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.operators.Operator
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Implementation of the [CLIPTextFactory] [ExternalAnalyser], which derives the CLIP feature from an [TextContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPTextFactory : ExternalWithFloatVectorDescriptorAnalyser<TextContent>() {
    override val analyserName: String = "CLIPText"
    override val contentClass = TextContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    // Default values for external API
    override val endpoint: String = "/extract/clip_text"
    override val host: String = "localhost"
    override val port: Int = 8888

    // Size and list for prototypical descriptor
    override val size = 512
    override val featureList = List(size) { 0.0f }


    /**
     * Requests the CLIP feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the CLIP feature descriptor.
     * @return A list of CLIP feature descriptors.
     */
    override fun requestDescriptor(content: ContentElement<*>): List<Float> {
        return httpRequest(content)
    }

    /**
     * Performs an HTTP request to the external feature API to obtain the CLIP feature for the given content element.
     *
     * @param content The [ContentElement] for which to obtain the CLIP feature.
     * @return A list containing the CLIP descriptor.
     */
    /*fun httpRequest(content: ContentElement<*>): List<Float> {
        val textContent = content as TextContent
        val url = "http://$host:$port$endpoint"
        val base64 = encodeTextToBase64(textContent.getContent())

        // Construct the request body
        val requestBody = URLEncoder.encode("data:text/plain;charset=utf-8,$base64", StandardCharsets.UTF_8.toString())

        return executeApiRequest(url, requestBody)
    }*/


    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIPTextFactory].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), featureList, true)


    override fun analyse(content: Collection<TextContent>): DescriptorList<FloatVectorDescriptor> {
        return processContent(content)
    }

    override fun newExtractor(
        field: Schema.Field<TextContent, FloatVectorDescriptor>, input: Operator<Ingested>, persisting: Boolean
    ): CLIPTextExtractor {
        require(field.analyser == this) { "" }
        return CLIPTextExtractor(field, input, persisting)
    }

    override fun newRetriever(
        field: Schema.Field<TextContent, FloatVectorDescriptor>, content: Collection<TextContent>
    ): CLIPTextRetriever {
        require(field.analyser == this) { }
        return newRetriever(field, this.analyse(content))
    }

    override fun newRetriever(
        field: Schema.Field<TextContent, FloatVectorDescriptor>, descriptors: DescriptorList<FloatVectorDescriptor>
    ): CLIPTextRetriever {
        require(field.analyser == this) { }
        return CLIPTextRetriever(field, descriptors.first())
    }

    companion object {
        /**
         * Static method to request the CLIP feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the CLIP feature descriptor.
         * @return A list of CLIP feature descriptors.
         */
        fun requestDescriptor(content: ContentElement<*>): List<Float> {
            return CLIPTextFactory().httpRequest(content)
        }


    }


}