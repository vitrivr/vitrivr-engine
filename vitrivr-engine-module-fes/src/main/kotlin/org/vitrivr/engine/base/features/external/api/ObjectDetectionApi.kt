package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.ObjectDetectionApi
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.openapitools.client.models.ObjectDetectionInput
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for object detection.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ObjectDetectionApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<ImageContent, List<Value.String>>(host, model, timeoutMs, pollingIntervalMs, retries) {

    /** The API used for FES object detection. */
    private val objectDetectionApi by lazy { ObjectDetectionApi(baseUrl = this.host, httpClientConfig = this.httpClientConfig) }

    /**
     * This method is used to start an object detection job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: ImageContent): JobStatus {
        logger.debug { "Starting object detection job for image." }
        val wrapped = ObjectDetectionInput(input.toDataUrl())
        return try {
            this.objectDetectionApi.newJobApiTasksObjectDetectionModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start object detection job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of an object detection job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<List<Value.String>> = try {
        this.objectDetectionApi.getJobResultsApiTasksObjectDetectionJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.labels?.map { Value.String(it) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of object detection job." }
        JobResult(JobState.failed, null)
    }
}