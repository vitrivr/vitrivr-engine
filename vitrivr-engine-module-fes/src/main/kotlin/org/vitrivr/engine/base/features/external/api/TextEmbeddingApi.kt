package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.TextEmbeddingApi
import org.openapitools.client.models.BatchedTextEmbeddingInput
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.openapitools.client.models.TextEmbeddingInput
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for text embedding.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class TextEmbeddingApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<TextContent, Value.FloatVector>(host, model, timeoutMs, pollingIntervalMs, retries) {

    /** The API used for FES text embedding. */
    private val textEmbeddingApi by lazy { TextEmbeddingApi(baseUrl = this.host, httpClientConfig = this.httpClientConfig) }

    /**
     * This method is used to start an text embedding job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: TextContent): JobStatus {
        val wrapped = TextEmbeddingInput(input.content)
        return try {
            logger.debug { "Starting text embedding for text '${input.content}'." }
            this.textEmbeddingApi.newJobApiTasksTextEmbeddingModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start text embedding job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to start a batched text embedding job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startBatchedJob(input: List<TextContent>): JobStatus {
        val wrapped = BatchedTextEmbeddingInput(input.map { it.content })
        return try {
            logger.debug { "Starting batched text embedding for texts." }
            this.textEmbeddingApi.newBatchedJobApiTasksTextEmbeddingBatchedModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start batched text embedding job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of an text embedding job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.FloatVector> = try {
        this.textEmbeddingApi.getJobResultsApiTasksTextEmbeddingJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.embedding?.let { r -> Value.FloatVector(FloatArray(r.size) { i -> r[i].toFloat() }) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of text embedding job." }
        JobResult(JobState.failed, null)
    }

    /**
     * This method is used to poll for results of a batched text embedding job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollBatchedJob(jobId: String): JobResult<List<Value.FloatVector>> = try {
        this.textEmbeddingApi.getBatchedJobResultsApiTasksTextEmbeddingBatchedJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.map { r -> Value.FloatVector(FloatArray(r.embedding.size) { i -> r.embedding[i].toFloat() }) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of batched text embedding job." }
        JobResult(JobState.failed, null)
    }
}