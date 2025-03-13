package org.vitrivr.engine.index.util.boundaryFile

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.IndexContext
import java.nio.file.Path
import java.time.Duration
import java.util.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

private val logger: KLogger = KotlinLogging.logger {}

class ApiShotBoundaryProvider: ShotBoundaryProviderFactory {

    override fun newShotBoundaryProvider(name: String, context: Context): ShotBoundaryProvider {
        val boundaryEndpointUri = context[name, "boundaryEndpointUri"]
            ?: throw IllegalArgumentException("Property 'boundaryFilesPath' must be specified")
        val toNanoScale = context[name, "toNanoScale"]?.toDouble()
            ?: throw IllegalArgumentException("Property 'toNanoScale' must be specified")
        return Instance<ShotBoundaryDetectionDescriptor>(boundaryEndpointUri, toNanoScale)
    }

    class Instance<T : MediaSegmentDecriptable>(
        private val boundaryEndpointUri: String,
        private val toNanoScale: Double = 1e9
    ) : ShotBoundaryProvider {

        companion object {
            private val client: HttpClient = HttpClient()
        }

        private suspend fun querySb(boundaryId: String): HttpResponse {
            // Api Call
            val response: HttpResponse = client.request("$boundaryEndpointUri?id=$boundaryId") {
                // Configure request parameters exposed by HttpRequestBuilder
                method = HttpMethod.Get
            }
            return response
        }

        override fun decode(boundaryId: String): List<MediaSegmentDescriptor> {

            val result = runBlocking<T> {
                val response = (try {
                    querySb(boundaryId)
                } catch (e: ArithmeticException) {
                    logger.error { "Error while fetching shot boundary file $boundaryId" }
                } as HttpResponse)

                if (response.status != HttpStatusCode.OK) {
                    "Error while fetching shot boundary file $boundaryId".let {
                        logger.error { it }
                        throw IllegalArgumentException(it)
                    }
                }
                @Suppress("UNCHECKED_CAST")
                response.body<Any?>() as T
            }

            val mediaSegementDescriptors = result.toMediaSegmentDescriptors()

            return mediaSegementDescriptors
        }
    }
}