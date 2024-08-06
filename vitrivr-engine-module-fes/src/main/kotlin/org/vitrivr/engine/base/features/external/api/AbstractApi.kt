package org.vitrivr.engine.base.features.external.api

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.vitrivr.engine.base.features.external.api.model.JobResult

/**
 * Wrapper class for the external API.
 *
 * @author Fynn Faber
 * @version 1.0.0
 *
 * @param host The hostname of the API.
 * @param model The model to use for the API.
 * @param timeoutMs The timeout in seconds for the API calls.
 * @param pollingIntervalMs The interval in milliseconds to poll for the job status.
 * @param retries The number of retries for the job.
 */
abstract class AbstractApi<I, O>(protected val host: String, protected val model: String, protected val timeoutMs: Long, protected val pollingIntervalMs: Long, protected val retries: Int) {
    companion object {
        /** The [KLogger] for this class. */
        @JvmStatic
        protected val logger: KLogger = KotlinLogging.logger {}
    }

    /** The HTTP client configuration. */
    protected val httpClientConfig: HttpClientConfig<*>.() -> Unit = {
        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMs
            connectTimeoutMillis = timeoutMs
            socketTimeoutMillis = timeoutMs
        }
    }

    init {
        logger.info { "Initialized API wrapper with host: $host, model: $model, timeout: $timeoutMs seconds, polling interval: $pollingIntervalMs ms" }
    }

    /**
     * Performs analysis through this [AbstractApi].
     *
     * @param input The input [I] to analyse.
     */
    fun analyse(input: I): O? = runBlocking {
        var retriesLeft = retries
        outer@ while (retriesLeft > 0) {
            /* Start job. */
            val jobStatus = this@AbstractApi.startJob(input)
            if (jobStatus.status == JobState.failed) {
                retriesLeft -= 1
                continue
            }

            /* Poll for result. */
            var jobResult = this@AbstractApi.pollJob(jobStatus.id)
            inner@ while (jobResult.status != JobState.complete) {
                if (jobResult.status == JobState.failed) {
                    logger.error { "$model job on host $host with ID: ${jobStatus.id} failed." }
                    retriesLeft -= 1
                    continue@outer
                }

                logger.debug { "Waiting for $model job completion on host $host with ID ${jobStatus.id}. Current status: ${jobResult.status}" }
                delay(this@AbstractApi.pollingIntervalMs)
                jobResult = this@AbstractApi.pollJob(jobStatus.id)
            }

            /* Extract results. */
            val result = jobResult.result
            if (result == null) {
                logger.warn { "$model job on host $host with ID: ${jobStatus.id} returned no result." }
            } else {
                logger.info { "Job result: $result" }
            }

            /* Return results. */
            return@runBlocking result
        }
        null
    }

    /**
     * This method is used to start a job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    protected abstract suspend fun startJob(input: I): JobStatus

    /**
     * This method is used to poll for results of a job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    protected abstract suspend fun pollJob(jobId: String): JobResult<O>
}