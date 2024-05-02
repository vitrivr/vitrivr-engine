package org.vitrivr.engine.base.features.external

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.scalar.FloatDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.util.extension.toDataUrl
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Implementation of the [ExternalAnalyser], which derives external features from an [ContentElement] as [Descriptor].
 *
 * @param T Type of [ContentElement] that this external analyzer operates on.
 * @param U Type of [Descriptor] produced by this external analyzer.
 *
 * @see [Analyser]
 *
 * @author Rahel Arnold
 * @version 1.2.0
 */

abstract class ExternalAnalyser<T : ContentElement<*>, U : Descriptor> : Analyser<T, U> {
    companion object {
        /** Name of the host parameter */
        const val HOST_PARAMETER_NAME = "host"

        /** Default value of the grid_size parameter. */
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"

        /** Logger instance used by [ExternalAnalyser]. */
        protected val logger: KLogger = KotlinLogging.logger {}


        /**
         * Executes an API request to the given [url] with the specified [content] and returns the response as a list of floats.
         *
         * @param content The [ContentElement] to execute HTTP request for.
         * @param url The URL for the API request.
         * @return A list of floats representing the API response.
         */
        @JvmStatic
        protected inline fun <reified U: Descriptor> httpRequest(content: ContentElement<*>, url: String): U? {
            /* Base64 encoded the content ... */
            val base64 = when (content) {
                is TextContent -> content.toDataUrl()
                is ImageContent -> content.toDataUrl()
                else -> throw IllegalArgumentException("Unsupported content type")
            }

            /* Create an HttpURLConnection and set up the connection for a POST request */
            val connection = try {
                URI(url).toURL().openConnection() as HttpURLConnection
            } catch (e: Throwable) {
                logger.error(e) { "Failed to open HTTP connection to $url."}
                return null
            }


            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            logger.trace { "Initialised external API request to $url." }

            try {
                /* Write the request body to the output stream. */
                val requestBody = URLEncoder.encode("data:text/plain;charset=utf-8,$base64", StandardCharsets.UTF_8.toString())
                connection.outputStream.use {
                    it.write("data=$requestBody".toByteArray())
                    it.flush()
                    it.close()
                }
                logger.trace { "Wrote request: $requestBody" }

                /* Get the response code (optional, but useful for error handling). */
                val responseCode = connection.responseCode
                logger.trace{"Received response code: $responseCode"}

                // Read the response as a JSON string
                if (responseCode != HttpURLConnection.HTTP_OK) return null
                return connection.inputStream.use { stream ->
                    when(U::class) {
                        FloatDescriptor::class -> FloatVectorDescriptor(UUID.randomUUID(), null, Json.decodeFromStream<FloatArray>(stream).map { Value.Float(it) })
                        else -> null
                    } as U?
                }
            } catch (e: Throwable) {
                logger.error(e) {"An error occurred during external API call."}
                return null
            } finally {
                connection.disconnect()
            }
        }
    }
}
