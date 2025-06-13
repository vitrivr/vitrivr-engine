package org.vitrivr.engine.index.util.boundary.api

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import org.vitrivr.engine.core.context.Context
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.vitrivr.engine.index.util.boundary.jrs.ShotBoundaryDetectionDescriptor
import org.vitrivr.engine.index.util.boundary.jrs.ShotBoundaryDetectionSubmit
import org.vitrivr.engine.index.util.boundary.MediaSegmentDescriptor
import org.vitrivr.engine.index.util.boundary.ShotBoundaryProvider
import org.vitrivr.engine.index.util.boundary.ShotBoundaryProviderFactory

private val logger: KLogger = KotlinLogging.logger {}

/**
 * XReco specific implementation of a [ApiShotBoundaryProvider] that uses an API to detect shot boundaries.
 *
 * @author Raphael Waltenspühl
 * @version 1.1.0
 */
class ApiShotBoundaryProvider: ShotBoundaryProviderFactory {
    /**
     * Creates a new instance of [ShotBoundaryProvider] that uses the API to fetch shot boundaries.
     *
     * @param name The name of the provider
     * @param context A [Context] object containing configuration parameters for the provider
     * @return An instance of [ShotBoundaryProvider] that can use an API to detect shot boundaries
     */
    override fun newShotBoundaryProvider(name: String, context: Context): ShotBoundaryProvider {
        val boundaryEndpointUri = context[name, "boundaryEndpointUri"]
            ?: throw IllegalArgumentException("Property 'boundaryFilesPath' must be specified")
        val toNanoScale = context[name, "toNanoScale"]?.toDouble()
            ?: throw IllegalArgumentException("Property 'toNanoScale' must be specified")
        return Instance(boundaryEndpointUri, toNanoScale)
    }

    /**
     * An instance of the [ShotBoundaryProvider] that uses an API to fetch shot boundaries.
     *
     * @param boundaryEndpointUri The URI of the boundary endpoint
     * @param toNanoScale Scaling value to convert seconds to nanoseconds (default is 1e9)
     */
    class Instance(
        private val boundaryEndpointUri: String,
        private val toNanoScale: Double = 1e9
    ) : ShotBoundaryProvider {

        companion object {
            private val client: HttpClient = HttpClient()
        }

        private suspend fun submitSb(sourceUri: String): HttpResponse {
            // Api Call
            val response: HttpResponse = client.request("${boundaryEndpointUri}/jobs/job?video_url=$sourceUri") {
                // Configure request parameters exposed by HttpRequestBuilder
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
            }
            return response
        }

        private suspend fun pollSb(jobid: String): HttpResponse {
            // Api Call
            val response: HttpResponse = client.request("${boundaryEndpointUri}/jobs/status/$jobid") {
                // Configure request parameters exposed by HttpRequestBuilder
                method = HttpMethod.Get
            }
            return response
        }

        override fun decode(sourceUri: String): List<MediaSegmentDescriptor> {

            val result = runBlocking<ShotBoundaryDetectionDescriptor> {
                val responseId = submitSb(sourceUri)

                if (responseId.status != HttpStatusCode.OK) {
                    "Error while submitting shot boundary file $sourceUri".let {
                        logger.error { it }
                        throw IllegalArgumentException(it)
                    }
                }

               val jobId = Json.decodeFromString<ShotBoundaryDetectionSubmit>(responseId.body<String>()).data?.jobId
                    ?: throw IllegalArgumentException("Error while fetching shot boundary file $sourceUri")

                var r: ShotBoundaryDetectionDescriptor
                do {
                    val response = (try {
                        pollSb(jobId)
                    } catch (e: ArithmeticException) {
                        logger.error { "Error while fetching shot boundary file $sourceUri" }
                    } as HttpResponse)

                    if (response.status != HttpStatusCode.OK) {
                        "Error while fetching shot boundary file $sourceUri".let {
                            logger.error { it }
                            throw IllegalArgumentException(it)
                        }
                    }
                    r = Json.decodeFromString<ShotBoundaryDetectionDescriptor>(response.body<String>())
                } while (
                    r.data?.job?.status != "complete"
                )
                r
            }

            val mediaSegementDescriptors = result.toMediaSegmentDescriptors()

            return mediaSegementDescriptors
        }
    }
}