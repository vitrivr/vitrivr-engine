package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.FaceEmbeddingApi
import org.openapitools.client.models.FaceEmbeddingInput
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for face embedding.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FaceEmbeddingApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<ImageContent, Value.FloatVector>(host, model, timeoutMs, pollingIntervalMs, retries) {

    /** The API used for FES face embedding. */
    private val faceEmbeddingApi by lazy { FaceEmbeddingApi(baseUrl = host, httpClient = this.client) }

    /**
     * This method is used to start a face embedding job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: ImageContent): JobStatus {
        val wrapped = FaceEmbeddingInput(input.toDataUrl())
        return try {
            logger.debug { "Starting batched face embedding for image." }
            this.faceEmbeddingApi.newJobApiTasksFaceEmbeddingModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start face embedding job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of a face embedding job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.FloatVector> = try {
        this.faceEmbeddingApi.getJobResultsApiTasksFaceEmbeddingJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.embedding?.let { r -> Value.FloatVector(FloatArray(r.size) { i -> r[i].toFloat() }) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of face embedding job." }
        JobResult(JobState.failed, null)
    }
}