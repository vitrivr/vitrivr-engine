package org.vitrivr.engine.base.features.external.implementations.DINO

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
 * Implementation of the [DINOFactory] [ExternalAnalyser], which derives the DINO feature from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class DINOFactory : ExternalWithFloatVectorDescriptorAnalyser<ImageContent>() {
    override val analyserName: String = "DINO"
    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    // Default values for external API
    override val endpoint: String = "/extract/dino"
    override val host: String = "localhost"
    override val port: Int = 8888

    // Size and list for prototypical descriptor
    override val size = 384
    override val featureList = List(size) { 0.0f }


    /**
     * Requests the DINO feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the DINO feature descriptor.
     * @return A list of DINO feature descriptors.
     */
    override fun requestDescriptor(content: ContentElement<*>): List<Float> {
        val a = httpRequest(content)
        print(a)
        return a
    }

    /**
     * Performs an HTTP request to the external feature API to obtain the DINO feature for the given content element.
     *
     * @param content The [ContentElement] for which to obtain the DINO feature.
     * @return A list containing the DINO descriptor.
     */
    /*fun httpRequest(content: ContentElement<*>): List<Float> {
        val imgContent = content as ImageContent
        val url = "http://$host:$port$endpoint"
        val base64 = imgContent.getContent().toDataURL()
        val requestBody = URLEncoder.encode(base64, StandardCharsets.UTF_8.toString())
        return executeApiRequest(url, requestBody)
    }*/


    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [DINOFactory].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), featureList, true)


    override fun analyse(content: Collection<ImageContent>): DescriptorList<FloatVectorDescriptor> {
        return processContent(content)
    }

    override fun newExtractor(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, input: Operator<Ingested>, persisting: Boolean
    ): DINOExtractor {
        require(field.analyser == this) { "" }
        return DINOExtractor(field, input, persisting)
    }

    override fun newRetriever(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, content: Collection<ImageContent>
    ): DINORetriever {
        require(field.analyser == this) { }
        return newRetriever(field, this.analyse(content))
    }

    override fun newRetriever(
        field: Schema.Field<ImageContent, FloatVectorDescriptor>, descriptors: DescriptorList<FloatVectorDescriptor>
    ): DINORetriever {
        require(field.analyser == this) { }
        return DINORetriever(field, descriptors.first())
    }

    companion object {
        /**
         * Static method to request the DINO feature descriptor for the given [ContentElement].
         *
         * @param content The [ContentElement] for which to request the DINO feature descriptor.
         * @return A list of DINO feature descriptors.
         */
        fun requestDescriptor(content: ContentElement<*>): List<Float> {
            return DINOFactory().httpRequest(content)
        }


    }


}