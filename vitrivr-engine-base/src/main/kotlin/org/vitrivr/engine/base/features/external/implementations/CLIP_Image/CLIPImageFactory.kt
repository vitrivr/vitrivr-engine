package org.vitrivr.engine.base.features.external.implementations.CLIP_Image

import com.google.gson.Gson
import org.vitrivr.engine.base.features.external.ExternalAnalyser
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
 * Implementation of the [CLIPImageFactory] [ExternalAnalyser], which derives the CLIP feature from an [ImageContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPImageFactory : ExternalAnalyser<ImageContent, FloatVectorDescriptor>() {
    override val analyserName: String = "CLIPImage"
    override val contentClass = ImageContent::class
    override val descriptorClass = FloatVectorDescriptor::class
    override val featureName: String = "/extract/clip_image"
    override fun requestDescriptor(content: ContentElement<*>): List<Float> {
        return httpRequest(content)
    }

    fun httpRequest(content: ContentElement<*>): List<Float> {
        val imgContent = content as ImageContent
        val url = "http://$host:$port$featureName"
        val base64 = encodeImageToBase64(imgContent.getContent())

        // Create an HttpURLConnection
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Construct the request body
            val requestBody = "data=${URLEncoder.encode("image/png;base64,$base64", StandardCharsets.UTF_8.toString())}"

            // Write the request body to the output stream
            val outputStream: OutputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write("data=$requestBody")
            writer.flush()
            writer.close()
            outputStream.close()

            // Get the response code (optional, but useful for error handling)
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")

            // Read the response as a JSON string
            val responseJson = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                val response = inputStream.readLine()
                inputStream.close()
                response
            } else {
                null
            }

            // Parse the JSON string to List<Float> using Gson
            return if (responseJson != null) {
                try {
                    val gson = Gson()
                    gson.fromJson(responseJson, List::class.java) as List<Float>
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else {
                emptyList()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // TODO Handle exceptions as needed
        } finally {
            connection.disconnect()
        }
        return emptyList()
    }


    override val host: String = "localhost"
    override val port: Int = 8888

    val size = 512
    private val featureList = List(size) { 0.0f }.toFloatArray().asList()

    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIPImageFactory].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() =
        FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), featureList, true)


    override fun analyse(content: Collection<ImageContent>): DescriptorList<FloatVectorDescriptor> {
        val resultList = mutableListOf<FloatVectorDescriptor>()

        for (imageContent in content) {

            val featureVector = requestDescriptor(imageContent)

            val descriptor = FloatVectorDescriptor(
                UUID.randomUUID(),
                null,
                featureVector,
                true
            )

            resultList.add(descriptor)
        }

        return resultList.toDescriptorList()
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

        fun requestDescriptor(content: ContentElement<*>): List<Float> {
            return CLIPImageFactory().httpRequest(content)
        }


    }


}