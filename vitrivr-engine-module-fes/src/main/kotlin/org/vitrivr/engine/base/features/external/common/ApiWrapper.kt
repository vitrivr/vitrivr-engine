package org.vitrivr.engine.base.features.external.common

import org.openapitools.client.apis.ImageEmbeddingApi
import org.openapitools.client.apis.TextEmbeddingApi
import org.openapitools.client.models.*
import org.vitrivr.engine.core.util.extension.toDataURL
import java.awt.image.BufferedImage

data class JobResult<S>(
        val status: String,
        val result: S? // Consider specifying a more precise type if possible
)

class JobWrapper<T, S>(
        private val startJobFunc: (T) -> JobStatus,
        private val getJobResultFunc: (String) -> JobResult<S>
) {

    fun executeJob(inp: T): S {
        val jobStatus = startJobFunc(inp)
        var jobResult = getJobResultFunc(jobStatus.id)

        while (jobResult.status != "completed") {
            if (jobResult.status == "failed") {
                throw Exception("Job failed")
            }
            // Wait for a while before checking the job status again
            Thread.sleep(1000)
            jobResult = getJobResultFunc(jobStatus.id)
        }

        return jobResult.result ?: throw Exception("Job result is null")
    }
}

class ApiWrapper(private val hostName:String, private val model: String) {

    private val textEmbeddingApi = TextEmbeddingApi(basePath = hostName)
    private val imageEmbeddingApi = ImageEmbeddingApi(basePath = hostName)
    fun textEmbedding(text: String): kotlin.collections.List<java.math.BigDecimal> {

        val input = TextEmbeddingInput(text)
        val job = JobWrapper<TextEmbeddingInput, TextEmbeddingOutput>(
                startJobFunc = { inp ->
                    textEmbeddingApi.newJobApiTasksTextEmbeddingModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    textEmbeddingApi.getJobResultsApiTasksTextEmbeddingJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).embedding
    }

    fun imageEmbedding(image: BufferedImage): kotlin.collections.List<java.math.BigDecimal> {
        val input = ImageEmbeddingInput(image.toDataURL())
        val job = JobWrapper<ImageEmbeddingInput, ImageEmbeddingOutput>(
                startJobFunc = { inp ->
                    imageEmbeddingApi.newJobApiTasksImageEmbeddingModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageEmbeddingApi.getJobResultsApiTasksImageEmbeddingJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).embedding
    }
}