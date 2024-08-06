package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.ImageCaptioningApi
import org.openapitools.client.models.ImageCaptioningInput
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for image captioning.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ImageCaptioningApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<ImageContent, Value.String>(host, model, timeoutMs, pollingIntervalMs, retries) {
    /** The API used for FES image captioning. */
    private val imageCaptioningApi by lazy { ImageCaptioningApi(baseUrl = this.host, httpClientConfig = this.httpClientConfig) }

    /**
     * This method is used to start an image captioning job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: ImageContent): JobStatus {
        logger.debug { "Starting image captioning job for image." }
        val wrapped = ImageCaptioningInput(input.toDataUrl())
        return try {
            this.imageCaptioningApi.newJobApiTasksImageCaptioningModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start image captioning job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of an image captioning job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.String> = try {
        this.imageCaptioningApi.getJobResultsApiTasksImageCaptioningJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.caption?.let { Value.String(it.trim()) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of image captioning job." }
        JobResult(JobState.failed, null)
    }
}