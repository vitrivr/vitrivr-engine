package org.vitrivr.engine.module.features.feature.external.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.util.extension.toDataURL
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
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

    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * Executes an API request to the given [url] with the specified [requestBody] and returns the response as a list of floats.
     *
     * @param url The URL for the API request.
     * @param requestBody The body of the API request.
     * @return A list of floats representing the API response.
     */
    private fun executeApiRequest(content: ContentElement<*>, url: String): List<Value.Float> {

        val base64 = when (content) {
            is TextContent -> Base64.getEncoder().encodeToString(content.content.toByteArray(StandardCharsets.UTF_8))
            is ImageContent -> content.content.toDataURL()
            else -> throw IllegalArgumentException("Unsupported content type")
        }

        val requestBody = URLEncoder.encode("data:text/plain;charset=utf-8,$base64", StandardCharsets.UTF_8.toString())

        // Create an HttpURLConnection
        val connection = URI(url).toURL().openConnection() as HttpURLConnection

        try {
            // Set up the connection for a POST request
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            logger.debug { "Initialised external API request" }

            // Write the request body to the output stream
            val outputStream: OutputStream = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write("data=$requestBody")
            writer.flush()
            writer.close()
            outputStream.close()

            logger.debug { "Wrote request: $requestBody" }

            // Get the response code (optional, but useful for error handling)
            val responseCode = connection.responseCode

            logger.debug{"Received response code: $responseCode"}

            // Read the response as a JSON string
            val responseJson = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                val response = inputStream.readLine()
                inputStream.close()
                logger.trace { "Received $response" }
                response
            } else {
                logger.warn { "Non OK response" }
                null
            }


            // Parse the JSON string to List<Float> using Gson
            return if (responseJson != null) {
                try {
                    Json.decodeFromString(ListSerializer(Float.serializer()), responseJson).map { Value.Float(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    logger.catching(e)
                    logger.warn { "Exception during json decode. Sending empty list" }
                    emptyList()
                }
            } else {
                logger.warn { "No response. Sending empty list" }
                emptyList()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            logger.catching(e)
            logger.error { "An error occurred during external API call, $e" }
        } finally {
            connection.disconnect()
            logger.trace { "Disconnected" }
        }
        return emptyList()
    }
}
