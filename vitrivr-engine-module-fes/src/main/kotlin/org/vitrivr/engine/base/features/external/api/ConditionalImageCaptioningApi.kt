package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.ConditionalImageCaptioningApi
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
class ConditionalImageCaptioningApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<Pair<ImageContent, TextContent>, Value.String>(host, model, timeoutMs, pollingIntervalMs, retries) {
    /** The API used for FES conditional image captioning. */
    private val conditionalImageCaptioningApi by lazy { ConditionalImageCaptioningApi(baseUrl = this.host, httpClientConfig = this.httpClientConfig) }

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
            this.conditionalImageCaptioningApi.newJobApiTasksConditionalImageCaptioningModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start conditional image captioning job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of a conditional image captioning job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.String> = try {
        this.conditionalImageCaptioningApi.getJobResultsApiTasksConditionalImageCaptioningJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.caption?.let { Value.String(it.trim()) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of conditional image captioning job." }
        JobResult(JobState.failed, null)
    }
}