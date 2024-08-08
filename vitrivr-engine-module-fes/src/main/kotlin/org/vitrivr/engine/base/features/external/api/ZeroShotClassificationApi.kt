package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.ZeroShotImageClassificationApi
import org.openapitools.client.models.BatchedZeroShotImageClassificationInput
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.openapitools.client.models.ZeroShotImageClassificationInput
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for zero shot image classification.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ZeroShotClassificationApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<Pair<ImageContent, List<String>>, List<Value.Double>>(host, model, timeoutMs, pollingIntervalMs, retries) {

    /** The API used for FES zero shot image classification. */
    private val zeroShotImageClassificationApi by lazy { ZeroShotImageClassificationApi(baseUrl = this.host, httpClientConfig = this.httpClientConfig) }

    /**
     * This method is used to start an  zero shot image classification job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: Pair<ImageContent, List<String>>): JobStatus {
        logger.debug { "Starting  zero shot image classification job for image." }
        val wrapped = ZeroShotImageClassificationInput(input.first.toDataUrl(), input.second)
        return try {
            this.zeroShotImageClassificationApi.newJobApiTasksZeroShotImageClassificationModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start  zero shot image classification job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to start a batched  zero shot image classification job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startBatchedJob(input: List<Pair<ImageContent, List<String>>>): JobStatus {
        logger.debug { "Starting batched  zero shot image classification job for images." }
        val classes = input.map { it.second }.toSet()
        if (classes.size > 1) {
            throw IllegalArgumentException("All classes must be the same for batched zero shot image classification.")
        }
        val wrapped = BatchedZeroShotImageClassificationInput(input.map { it.first.toDataUrl() }, classes.first())
        return try {
            this.zeroShotImageClassificationApi.newBatchedJobApiTasksZeroShotImageClassificationBatchedModelJobsPost(
                this.model,
                wrapped
            ).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start batched  zero shot image classification job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of an object detection job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<List<Value.Double>> = try {
        this.zeroShotImageClassificationApi.getJobResultsApiTasksZeroShotImageClassificationJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.probabilities?.map { Value.Double(it) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of object detection job." }
        JobResult(JobState.failed, null)
    }

    /**
     * This method is used to poll for results of a batched object detection job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollBatchedJob(jobId: String): JobResult<List<List<Value.Double>>> = try {
        this.zeroShotImageClassificationApi.getBatchedJobResultsApiTasksZeroShotImageClassificationBatchedJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.map { it.probabilities.map { Value.Double(it) } })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of batched object detection job." }
        JobResult(JobState.failed, null)
    }
}