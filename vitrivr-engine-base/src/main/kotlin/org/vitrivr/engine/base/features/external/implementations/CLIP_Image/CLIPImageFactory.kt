package org.vitrivr.engine.base.features.external.implementations.CLIP_Image

import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.base.features.external.common.ExternalWithFloatVectorDescriptorAnalyser
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.util.extension.toDataURL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Implementation of the [CLIPImageFactory] [ExternalAnalyser], which derives the CLIP feature from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPImageFactory : ExternalWithFloatVectorDescriptorAnalyser<ImageContent>() {
    override val analyserName: String = "CLIPImage"
    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    // Default values for external API
    override val endpoint: String = "/extract/clip_image"
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
        val imgContent = content as ImageContent
        val url = "http://$host:$port$endpoint"
        val base64 = imgContent.getContent().toDataURL()
        val requestBody = URLEncoder.encode(base64, StandardCharsets.UTF_8.toString())

        return executeApiRequest(url, requestBody)
    }*/


    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIPImageFactory].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), featureList, true)


    override fun analyse(content: Collection<ImageContent>): DescriptorList<FloatVectorDescriptor> {
        return processContent(content)
    }

    override fun newExtractor(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Ingested>, persisting: Boolean
    ): CLIPImageExtractor {
        require(field.analyser == this) { "" }
        return CLIPImageExtractor(field, input, persisting)
    }

    override fun newRetriever(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, content: Collection<ImageContent>
    ): CLIPImageRetriever {
        require(field.analyser == this) { }
        return newRetriever(field, this.analyse(content))
    }

    override fun newRetriever(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, descriptors: DescriptorList<FloatVectorDescriptor>
    ): CLIPImageRetriever {
        require(field.analyser == this) { }
        return CLIPImageRetriever(field, descriptors.first())
    }

    companion object {
        /**
         * Static method to request the CLIP feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the CLIP feature descriptor.
         * @return A list of CLIP feature descriptors.
         */
        fun requestDescriptor(content: ContentElement<*>): List<Float> {
            return CLIPImageFactory().httpRequest(content)
        }


    }


}