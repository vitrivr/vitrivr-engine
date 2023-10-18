package org.vitrivr.engine.base.features.external.implementations.DINO

import com.google.gson.Gson
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.base.features.external.common.ExternalWithFloatVectorDescriptorAnalyser
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.model.util.toDescriptorList
import org.vitrivr.engine.core.operators.Operator
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.imageio.ImageIO

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

    // Size and feature list for prototypical descriptor
    val size = 384
    private val featureList = List(size) { 0.0f }

    /**
     * Requests the DINO feature descriptor for the given [ContentElement].
     *
     * @param content The [ContentElement] for which to request the DINO feature descriptor.
     * @return A list of DINO feature descriptors.
     */
    override fun requestDescriptor(content: ContentElement<*>): List<Float> {
        return httpRequest(content)
    }

    /**
     * Performs an HTTP request to the external feature API to obtain the DINO feature for the given content element.
     *
     * @param content The [ContentElement] for which to obtain the DINO feature.
     * @return A list containing the DINO descriptor.
     */
    fun httpRequest(content: ContentElement<*>): List<Float> {
        val imgContent = content as ImageContent
        val url = "http://$host:$port$endpoint"
        val base64 = encodeImageToBase64(imgContent.getContent())

        // Construct the request body
        val requestBody = "data=${URLEncoder.encode("image/png;base64,$base64", StandardCharsets.UTF_8.toString())}"

        return executeApiRequest(url, requestBody)
    }


    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [DINOFactory].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), featureList, true)


    override fun analyse(content: Collection<ImageContent>): DescriptorList<FloatVectorDescriptor> {
        val resultList = mutableListOf<FloatVectorDescriptor>()

        for (imageContent in content) {

            val featureVector = requestDescriptor(imageContent)

            val descriptor = FloatVectorDescriptor(
                UUID.randomUUID(), null, featureVector, true
            )

            resultList.add(descriptor)
        }

        return resultList.toDescriptorList()
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
         * Encodes a BufferedImage to base64.
         *
         * @param bufferedImage The BufferedImage to encode.
         * @return The base64-encoded string.
         */
        fun encodeImageToBase64(bufferedImage: BufferedImage): String {
            val byteArrayOutputStream = ByteArrayOutputStream()

            try {
                // Write the BufferedImage to the output stream
                ImageIO.write(bufferedImage, "png", byteArrayOutputStream)

                // Convert the output stream to a byte array
                val imageBytes = byteArrayOutputStream.toByteArray()

                // Encode the byte array to base64
                return Base64.getEncoder().encodeToString(imageBytes)
            } catch (e: IOException) {
                e.printStackTrace()
                // TODO Handle the exception as needed
                return ""
            } finally {
                try {
                    byteArrayOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

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