package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.ImageEmbeddingApi
import org.openapitools.client.models.ImageEmbeddingInput
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for image embedding.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ImageEmbeddingApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<ImageContent, Value.FloatVector>(host, model, timeoutMs, pollingIntervalMs, retries) {

    /** The API used for FES image embedding. */
    private val imageEmbeddingApi by lazy { ImageEmbeddingApi(baseUrl = host, httpClient = client) }

    /**
     * This method is used to start an image embedding job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: ImageContent): JobStatus {
        val wrapped = ImageEmbeddingInput(input.toDataUrl())
        return try {
            logger.debug { "Starting image embedding for image." }
            this.imageEmbeddingApi.newJobApiTasksImageEmbeddingModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start image embedding job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of an image embedding job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.FloatVector> = try {
        this.imageEmbeddingApi.getJobResultsApiTasksImageEmbeddingJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.embedding?.let { r -> Value.FloatVector(FloatArray(r.size) { i -> r[i].toFloat() }) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of image embedding job." }
        JobResult(JobState.failed, null)
    }
}