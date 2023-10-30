package org.vitrivr.engine.base.features.external.common

import com.google.gson.Gson
import org.vitrivr.engine.base.features.external.ExternalAnalyser
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.util.extension.toDataURL
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Abstract base class for external analyzers that generate [FloatVectorDescriptor] from [ContentElement].
 *
 * @param C Type parameter for the [ContentElement] being analyzed.
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalWithFloatVectorDescriptorAnalyser<C : ContentElement<*>> : ExternalAnalyser<C, FloatVectorDescriptor>() {

    /**
     * Size of the feature vector.
     */
    abstract val size: Int

    /**
     * Default feature list initialized with zeros.
     */
    abstract val featureList: List<Float>

    /**
     * Executes an API request to the given [url] with the specified [requestBody] and returns the response as a list of floats.
     *
     * @param url The URL for the API request.
     * @param requestBody The body of the API request.
     * @return A list of floats representing the API response.
     */
    private fun executeApiRequest(url: String, requestBody: String): List<Float> {
        // Create an HttpURLConnection
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

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

            print(featureList.size)

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

    abstract override fun requestDescriptor(content: ContentElement<*>): List<Float>

    /**
     * Processes a collection of [ContentElement] items and generates a [DescriptorList] of [FloatVectorDescriptor].
     *
     * @param content The collection of [ContentElement] items to process.
     * @return [List] of [FloatVectorDescriptor].
     */
    fun processContent(content: Collection<*>): List<FloatVectorDescriptor> {
        val resultList = mutableListOf<FloatVectorDescriptor>()

        for (item in content) {
            val featureVector = requestDescriptor(item as ContentElement<*>)

            val descriptor = FloatVectorDescriptor(
                UUID.randomUUID(), null, featureVector, true
            )

            resultList.add(descriptor)
        }
        return resultList
    }

    fun httpRequest(content: ContentElement<*>): List<Float> {
        val url = "http://$host:$port$endpoint"
        val base64 = when (content) {
            is TextContent -> encodeTextToBase64(content.content)
            is ImageContent -> content.content.toDataURL()
            else -> throw IllegalArgumentException("Unsupported content type")
        }

        val requestBody = constructRequestBody(base64)
        return executeApiRequest(url, requestBody)
    }

    private fun constructRequestBody(base64: String): String {
        return URLEncoder.encode("data:text/plain;charset=utf-8,$base64", StandardCharsets.UTF_8.toString())
    }

    /**
     * Encodes a text to base64.
     *
     * @param text The string to encode.
     * @return The base64-encoded string.
     */
    private fun encodeTextToBase64(text: String): String {
        val textBytes = text.toByteArray(StandardCharsets.UTF_8)

        // Encode the byte array to base64
        return Base64.getEncoder().encodeToString(textBytes)
    }


}