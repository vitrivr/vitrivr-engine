package org.vitrivr.engine.module.features.feature.external

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.types.Value
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

/** Logger instance used by [ExternalAnalyser]. */
val logger: KLogger = KotlinLogging.logger {}

abstract class ExternalAnalyser<T : ContentElement<*>, U : Descriptor<*>> : Analyser<T, U> {
    companion object {
        /** Name of the host parameter */
        const val HOST_PARAMETER_NAME = "host"

        /** Default value of the grid_size parameter. */
        const val HOST_PARAMETER_DEFAULT = "http://localhost:8888/"




        /**
         * Executes an API request to the given [url] with the specified [content] and returns the response as a list of floats.
         *
         * @param content The [ContentElement] to execute HTTP request for.
         * @param url The URL for the API request.
         * @return A list of floats representing the API response.
         */
        @OptIn(ExperimentalSerializationApi::class)
        @JvmStatic
        protected inline fun <reified U : Descriptor<*>> httpRequest(
            url: String,
            requestBody: String,
            contentType: String = "application/x-www-form-urlencoded",
            headers: Map<String, String> = emptyMap()
        ): U? = runBlocking{
            val body = requestBody.toByteArray(StandardCharsets.UTF_8)
            val client = try {
                HttpClient(CIO) {
                    install(HttpRequestRetry) {
                        retryOnServerErrors(maxRetries = 5)
                        exponentialDelay()
                    }
                    defaultRequest {
                        headers.forEach { (key, value) -> header(key, value) }
                        header("Content-Type", contentType)
                    }
                }
            } catch (e: Throwable) {
                logger.error(e) { "Failed to initialize Ktor HTTP client for $url." }
                return@runBlocking null
            }



            try {
                val response = client.request(url) {
                    method = HttpMethod.Post
                    setBody(body)
                }

                logger.trace { "Receiving response with status ${response.status}" }

                if (!response.status.isSuccess()) {
                    logger.warn { "Non-success response code: ${response.status.value} from $url" }
                    null
                } else {
                    response.bodyAsChannel().toInputStream().use { stream ->
                        when (U::class) {
                            FloatVectorDescriptor::class -> FloatVectorDescriptor(
                                UUID.randomUUID(),
                                null,
                                Value.FloatVector(Json.decodeFromStream<FloatArray>(stream))
                            )

                            else -> null
                        } as U?
                    }
                }
            } catch (e: Throwable) {
                logger.error(e) { "An error occurred during the external API call to $url." }
                null
            } finally {
                client.close()
            }
        }
    }
}