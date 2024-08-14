package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.ConditionalImageCaptioningApi
import org.openapitools.client.infrastructure.map
import org.openapitools.client.models.BatchedConditionalImageCaptioningInput
import org.openapitools.client.models.ConditionalImageCaptioningInput
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for conditional image captioning.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ConditionalImageCaptioningApi(
    host: String,
    model: String,
    timeoutMs: Long,
    pollingIntervalMs: Long,
    retries: Int
) : AbstractApi<Pair<ImageContent, TextContent>, Value.Text>(host, model, timeoutMs, pollingIntervalMs, retries) {
    /** The API used for FES conditional image captioning. */
    private val conditionalImageCaptioningApi by lazy {
        ConditionalImageCaptioningApi(
            baseUrl = this.host,
            httpClientConfig = this.httpClientConfig
        )
    }

    /**
     * This method is used to start a conditional image captioning job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: Pair<ImageContent, TextContent>): JobStatus {
        logger.debug { "Starting conditional image captioning job for image." }
        val wrapped = ConditionalImageCaptioningInput(input.first.toDataUrl(), input.second.content)
        return try {
            this.conditionalImageCaptioningApi.newJobApiTasksConditionalImageCaptioningModelJobsPost(
                this.model,
                wrapped
            ).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start conditional image captioning job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to start a batched conditional image captioning job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startBatchedJob(input: List<Pair<ImageContent, TextContent>>): JobStatus {
        logger.debug { "Starting batched conditional image captioning job for images." }
        val wrapped = BatchedConditionalImageCaptioningInput(image = input.map { it.first.toDataUrl() },
            text = input.map { it.second.content })
        return try {
            val result =
                this.conditionalImageCaptioningApi.newBatchedJobApiTasksConditionalImageCaptioningBatchedModelJobsPost(
                    this.model,
                    wrapped
                )
            return result.takeIf { it.success }?.body()
                ?: throw IllegalStateException("Api Error. Status: ${result.response.status}")
        } catch (ex: Throwable) {
            logger.error(ex) { "Error in startBatchedJob" }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of a conditional image captioning job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.Text> = try {
        this.conditionalImageCaptioningApi.getJobResultsApiTasksConditionalImageCaptioningJobsJobGet(jobId).body()
            .let { result ->
                val value = result.result?.caption?.trim()
                if (!value.isNullOrBlank()) {
                    JobResult(result.status, Value.Text(value))
                } else {
                    JobResult(result.status, null)
                }
            }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of conditional image captioning job." }
        JobResult(JobState.failed, null)
    }

    /**
     * This method is used to poll for results of a batched conditional image captioning job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollBatchedJob(jobId: String): JobResult<List<Value.Text>> {
        this.conditionalImageCaptioningApi.getBatchedJobResultsApiTasksConditionalImageCaptioningBatchedJobsJobGet(
            jobId
        ).body().let { result ->
            val value = result.result?.map { it.caption.trim() }
            if (value != null) {
                return JobResult(result.status, value.map { Value.Text(it) })
            } else {
                return JobResult(result.status, null)
            }
        }
    }
}