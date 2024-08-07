package org.vitrivr.engine.base.features.external.api

import org.openapitools.client.apis.AutomatedSpeechRecognitionApi
import org.openapitools.client.models.AutomatedSpeechRecognitionInput
import org.openapitools.client.models.JobState
import org.openapitools.client.models.JobStatus
import org.vitrivr.engine.base.features.external.api.model.JobResult
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.types.Value

/**
 * An [AbstractApi] for automated speaker recognition (ASR).
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class AsrApi(host: String, model: String, timeoutMs: Long, pollingIntervalMs: Long, retries: Int) : AbstractApi<AudioContent, Value.Text>(host, model, timeoutMs, pollingIntervalMs, retries) {

    /** The API used for FES ASR. */
    private val automatedSpeechRecognitionApi by lazy { AutomatedSpeechRecognitionApi(baseUrl = this.host, httpClientConfig = this.httpClientConfig) }

    /**
     * This method is used to start an ASR job on the API.
     *
     * @param input The input for the job.
     * @return The [JobStatus]
     */
    override suspend fun startJob(input: AudioContent): JobStatus {
        logger.debug { "Starting ASR job for audio." }
        val wrapped = AutomatedSpeechRecognitionInput(input.toDataURL())
        return try {
            this.automatedSpeechRecognitionApi.newJobApiTasksAutomatedSpeechRecognitionModelJobsPost(this.model, wrapped).body()
        } catch (e: Throwable) {
            JobStatus("unknown", JobState.failed)
        }
    }

    /**
     * This method is used to poll for results of an ASR job on the API.
     *
     * @param jobId The ID of the job to poll.
     * @return The [JobResult]
     */
    override suspend fun pollJob(jobId: String): JobResult<Value.Text> = try {
        this.automatedSpeechRecognitionApi.getJobResultsApiTasksAutomatedSpeechRecognitionJobsJobGet(jobId).body().let { result ->
            val value = result.result?.transcript?.trim()
            if (!value.isNullOrBlank()) {
                JobResult(result.status, Value.Text(value))
            } else {
                JobResult(result.status, null)
            }
        }
    } catch (e: Throwable) {
        JobResult(JobState.failed, null)
    }
}