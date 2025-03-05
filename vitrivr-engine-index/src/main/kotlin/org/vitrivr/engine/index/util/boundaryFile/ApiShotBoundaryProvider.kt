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

class ApiShotBoundaryProvider : ShotBoundaryProviderFactory {

    override fun newShotBoundaryProvider(name: String, context: Context): ShotBoundaryProvider {
        val boundaryEndpointUri = context[name, "boundaryEndpointUri"]
            ?: throw IllegalArgumentException("Property 'boundaryFilesPath' must be specified")
        val toNanoScale = context[name, "toNanoScale"]?.toDouble()
            ?: throw IllegalArgumentException("Property 'toNanoScale' must be specified")
        return Instance(boundaryEndpointUri, toNanoScale)
    }

    class Instance(
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

            val mediaSegementDescriptors = mutableListOf<MediaSegmentDescriptor>()

            val result = runBlocking<String> {
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
                response.body<String>()
            }


            with(Path.of(this.boundaryEndpointUri).resolve("$boundaryId").toFile().bufferedReader()) {
                var shotCounter = 0
                while (true) {

                    var line: String = readLine() ?: break
                    line = line.trim()

                    when {
                        !line[0].isDigit() -> {
                            continue
                        }

                        line.split(" ", "\t").size < 2 -> {
                            continue
                        }

                        line.split(" ", "\t").size == 4 -> {
                            val (startframe, starttime, endframe, endtime) = line.split(" ", "\t")
                            mediaSegementDescriptors.add(
                                MediaSegmentDescriptor(
                                    boundaryId,
                                    UUID.randomUUID().toString(),
                                    shotCounter,
                                    startframe.toInt(),
                                    endframe.toInt(),
                                    Duration.ofNanos((starttime.toDouble() * toNanoScale).toLong()),
                                    Duration.ofNanos((endtime.toDouble() * toNanoScale).toLong()),
                                    true
                                )
                            )
                        }
                    }
                    shotCounter++
                }
            }

            return mediaSegementDescriptors
        }
    }
}