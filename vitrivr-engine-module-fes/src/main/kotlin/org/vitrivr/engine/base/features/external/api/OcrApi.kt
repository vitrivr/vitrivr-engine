package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.OpticalCharacterRecognitionApi
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.openapitools.client.models.OpticalCharacterRecognitionInput
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for optical character recognition (OCR).
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class OcrApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<ImageContent, Value.String>(host, model, timeoutMs, pollingIntervalMs, retries) {
    /** The API used for FES OCR. */
    private val opticalCharacterRecognitionApi by lazy { OpticalCharacterRecognitionApi(baseUrl = this.host, httpClientConfig = this.httpClientConfig) }

    /**
     * This method is used to start an OCR job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: ImageContent): JobStatus {
        logger.debug { "Starting OCR job for image." }
        val wrapped = OpticalCharacterRecognitionInput(input.toDataUrl())
        return try {
            this.opticalCharacterRecognitionApi.newJobApiTasksOpticalCharacterRecognitionModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to start OCR job." }
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of an OCR job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.String> = try {
        this.opticalCharacterRecognitionApi.getJobResultsApiTasksOpticalCharacterRecognitionJobsJobGet(jobId).body().let { result ->
            JobResult(result.status, result.result?.text?.let { Value.String(it.trim()) })
        }
    } catch (e: Throwable) {
        logger.error(e) { "Failed to poll for status of OCR job." }
        JobResult(JobState.failed, null)
    }
}