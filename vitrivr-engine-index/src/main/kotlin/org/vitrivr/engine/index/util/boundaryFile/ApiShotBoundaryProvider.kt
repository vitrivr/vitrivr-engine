package org.vitrivr.engine.index.util.boundaryFile

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
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.index.util.boundaryFile.JRS.ShotBoundaryDetectionDescriptor
import org.vitrivr.engine.index.util.boundaryFile.JRS.ShotBoundaryDetectionSubmit

private val logger: KLogger = KotlinLogging.logger {}

/**
 * TODO Remove this
 * XReco specific implementation of a [ApiShotBoundaryProvider] that uses an API to detect shot boundaries.
 */
class ApiShotBoundaryProvider: ShotBoundaryProviderFactory {

    override fun newShotBoundaryProvider(name: String, parameters: Map<String, String>, context: IndexContext): ShotBoundaryProvider {
        val boundaryEndpointUri = parameters["boundaryEndpointUri"]
            ?: throw IllegalArgumentException("Property 'boundaryFilesPath' must be specified")
        val toNanoScale = parameters["toNanoScale"]?.toDouble()
            ?: throw IllegalArgumentException("Property 'toNanoScale' must be specified")
        return Instance(boundaryEndpointUri, toNanoScale)
    }

    /**
     * Instance of the [ShotBoundaryProvider] that uses the API to fetch the shot boundaries.
     * @param boundaryEndpointUri The URI of the boundary endpoint
     * @param toNanoScale Scaling value to convert seconds to nanoseconds
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