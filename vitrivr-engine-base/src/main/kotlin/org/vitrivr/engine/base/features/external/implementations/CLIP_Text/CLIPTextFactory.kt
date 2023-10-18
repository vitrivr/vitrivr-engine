package org.vitrivr.engine.base.features.external.implementations.CLIP_Text

import com.google.gson.Gson
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.util.DescriptorList
import org.vitrivr.engine.core.model.util.toDescriptorList
import org.vitrivr.engine.core.operators.Operator
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Implementation of the [CLIPTextFactory] [ExternalAnalyser], which derives the CLIP feature from an [TextContent] as [FloatVectorDescriptor].
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
class CLIPTextFactory : ExternalAnalyser<TextContent, FloatVectorDescriptor>() {
    override val analyserName: String = "CLIPText"
    override val contentClass = TextContent::class
    override val descriptorClass = FloatVectorDescriptor::class

    // Default values for external API
    override val endpoint: String = "/extract/clip_text"
    override val host: String = "localhost"
    override val port: Int = 8888

    // Size and feature list for prototypical descriptor
    val size = 512
    private val featureList = List(size) { 0.0f }.toFloatArray().asList()

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
    fun httpRequest(content: ContentElement<*>): List<Float> {
        val textContent = content as TextContent
        val url = "http://$host:$port$endpoint"
        val base64 = encodeTextToBase64(textContent.getContent())

        // Create an HttpURLConnection
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Construct the request body
            val requestBody = "data=${URLEncoder.encode("data:text/plain;charset=utf-8,$base64", StandardCharsets.UTF_8.toString())}"

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


    /**
     * Generates a prototypical [FloatVectorDescriptor] for this [CLIPTextFactory].
     *
     * @return [FloatVectorDescriptor]
     */
    override fun prototype() = FloatVectorDescriptor(UUID.randomUUID(), UUID.randomUUID(), featureList, true)


    override fun analyse(content: Collection<TextContent>): DescriptorList<FloatVectorDescriptor> {
        val resultList = mutableListOf<FloatVectorDescriptor>()

        for (textContent in content) {

            val featureVector = requestDescriptor(textContent)

            val descriptor = FloatVectorDescriptor(
                UUID.randomUUID(), null, featureVector, true
            )

            resultList.add(descriptor)
        }

        return resultList.toDescriptorList()
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
         * Encodes a text to base64.
         *
         * @param text The string to encode.
         * @return The base64-encoded string.
         */
        fun encodeTextToBase64(text: String): String {
            val textBytes = text.toByteArray(StandardCharsets.UTF_8)

            // Encode the byte array to base64
            return Base64.getEncoder().encodeToString(textBytes)
        }
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