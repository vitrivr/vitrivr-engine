package org.vitrivr.engine.base.features.external.common

import org.openapitools.client.apis.*
import org.openapitools.client.models.*
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.util.extension.toDataURL
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

data class JobResult<S>(
        val status: JobState,
        val result: S?
)

class JobWrapper<T, S>(
        private val startJobFunc: (T) -> JobStatus,
        private val getJobResultFunc: (String) -> JobResult<S>
) {

    fun executeJob(inp: T): S {
        val jobStatus = startJobFunc(inp)
        var jobResult = getJobResultFunc(jobStatus.id)

        while (jobResult.status != JobState.complete) {
            if (jobResult.status == JobState.failed) {
                throw Exception("Job failed.")
            }
            // Wait for a while before checking the job status again
            Thread.sleep(1000)
            jobResult = getJobResultFunc(jobStatus.id)
        }

        return jobResult.result ?: throw Exception("Job result is null")
    }
}

fun AudioContent.toDataURL(): String {
    val data = this.content // Assuming this.content returns a ShortArray of audio data
    val buffer = ByteBuffer.allocate(44 + data.remaining() * 2).order(ByteOrder.LITTLE_ENDIAN)

    // Write WAV header
    writeWaveHeader(buffer, this.samplingRate.toFloat(), 1, data.remaining())

    // Write audio data
    while (data.hasRemaining()) {
        buffer.putShort(data.get())
    }

    val base64 = Base64.getEncoder().encodeToString(buffer.array())
    return "data:audio/wav;base64,$base64"
}

private fun writeWaveHeader(buffer: ByteBuffer, samplingRate: Float, channels: Short, length: Int) {
    val subChunk2Length = length * channels * (16 / 8) // Assuming 16 bits per sample

    // RIFF Chunk
    buffer.put("RIFF".toByteArray())
    buffer.putInt(36 + subChunk2Length)
    buffer.put("WAVE".toByteArray())

    // fmt chunk
    buffer.put("fmt ".toByteArray())
    buffer.putInt(16) // PCM header size
    buffer.putShort(1) // Audio format 1 = PCM
    buffer.putShort(channels)
    buffer.putInt(samplingRate.toInt())
    buffer.putInt((samplingRate * channels * (16 / 8)).toInt()) // Byte rate
    buffer.putShort((channels * (16 / 8)).toShort()) // Block align
    buffer.putShort(16) // Bits per sample

    // data chunk
    buffer.put("data".toByteArray())
    buffer.putInt(subChunk2Length)
}


class ApiWrapper(private val hostName:String, private val model: String) {

    private val textEmbeddingApi = TextEmbeddingApi(basePath = hostName)
    private val imageEmbeddingApi = ImageEmbeddingApi(basePath = hostName)
    private val imageCaptioningApi = ImageCaptioningApi(basePath = hostName)
    private val zeroShotImageClassificationApi = ZeroShotImageClassificationApi(basePath = hostName)
    private val conditionalImageCaptioningApi = ConditionalImageCaptioningApi(basePath = hostName)
    private val faceEmbeddingApi = FaceEmbeddingApi(basePath = hostName)
    private val objectDetectionApi = ObjectDetectionApi(basePath = hostName)
    private val automatedSpeechRecognitionApi = AutomatedSpeechRecognitionApi(basePath = hostName)
    private val opticalCharacterRecognitionApi = OpticalCharacterRecognitionApi(basePath = hostName)
    fun textEmbedding(text: String): kotlin.collections.List<Float> {

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
        return job.executeJob(input).embedding.map{it.toFloat()}
    }

    fun textEmbedding(text: kotlin.collections.List<String>): kotlin.collections.List<kotlin.collections.List<Float>> {
        val input = BatchedTextEmbeddingInput(text)
        val job = JobWrapper<BatchedTextEmbeddingInput, kotlin.collections.List<TextEmbeddingOutput>>(
                startJobFunc = { inp ->
                    textEmbeddingApi.newBatchedJobApiTasksTextEmbeddingBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    textEmbeddingApi.getBatchedJobResultsApiTasksTextEmbeddingBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        //map to list of embeddings
        return job.executeJob(input).map { it.embedding.map{it.toFloat()} }
    }

    fun imageEmbedding(image: BufferedImage): kotlin.collections.List<Float> {
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
        return job.executeJob(input).embedding.map{it.toFloat()}
    }

    fun imageEmbedding(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<kotlin.collections.List<Float>> {
        val input = BatchedImageEmbeddingInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedImageEmbeddingInput, kotlin.collections.List<ImageEmbeddingOutput>>(
                startJobFunc = { inp ->
                    imageEmbeddingApi.newBatchedJobApiTasksImageEmbeddingBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageEmbeddingApi.getBatchedJobResultsApiTasksImageEmbeddingBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).map { it.embedding.map{it.toFloat()}}
    }

    fun imageCaptioning(image: BufferedImage): String {
        val input = ImageCaptioningInput(image.toDataURL())
        val job = JobWrapper<ImageCaptioningInput, ImageCaptioningOutput>(
                startJobFunc = { inp ->
                    imageCaptioningApi.newJobApiTasksImageCaptioningModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageCaptioningApi.getJobResultsApiTasksImageCaptioningJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).caption
    }

    fun imageCaptioning(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<String> {
        val input = BatchedImageCaptioningInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedImageCaptioningInput, kotlin.collections.List<ImageCaptioningOutput>>(
                startJobFunc = { inp ->
                    imageCaptioningApi.newBatchedJobApiTasksImageCaptioningBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageCaptioningApi.getBatchedJobResultsApiTasksImageCaptioningBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).map { it.caption }
    }

    fun zeroShotImageClassification(image: BufferedImage, labels: kotlin.collections.List<String>): kotlin.collections.List<Float> {
        val input = ZeroShotImageClassificationInput(image.toDataURL(), labels)
        val job = JobWrapper<ZeroShotImageClassificationInput, ZeroShotImageClassificationOutput>(
                startJobFunc = { inp ->
                    zeroShotImageClassificationApi.newJobApiTasksZeroShotImageClassificationModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    zeroShotImageClassificationApi.getJobResultsApiTasksZeroShotImageClassificationJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).probabilities.map{it.toFloat()}
    }

    fun zeroShotImageClassification(image: kotlin.collections.List<BufferedImage>, labels: kotlin.collections.List<String>): kotlin.collections.List<kotlin.collections.List<Float>> {
        val input = BatchedZeroShotImageClassificationInput(image.map { it.toDataURL() }, labels)
        val job = JobWrapper<BatchedZeroShotImageClassificationInput, kotlin.collections.List<ZeroShotImageClassificationOutput>>(
                startJobFunc = { inp ->
                    zeroShotImageClassificationApi.newBatchedJobApiTasksZeroShotImageClassificationBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    zeroShotImageClassificationApi.getBatchedJobResultsApiTasksZeroShotImageClassificationBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).map { it.probabilities.map{it.toFloat()}}
    }

    fun conditionalImageCaptioning(image: BufferedImage, text: String): String {
        val input = ConditionalImageCaptioningInput(image.toDataURL(), text)
        val job = JobWrapper<ConditionalImageCaptioningInput, ConditionalImageCaptioningOutput>(
                startJobFunc = { inp ->
                    conditionalImageCaptioningApi.newJobApiTasksConditionalImageCaptioningModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    conditionalImageCaptioningApi.getJobResultsApiTasksConditionalImageCaptioningJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).caption
    }

    fun conditionalImageCaptioning(image: kotlin.collections.List<BufferedImage>, text: kotlin.collections.List<String>): kotlin.collections.List<String> {
        val input = BatchedConditionalImageCaptioningInput(image.map { it.toDataURL() }, text)
        val job = JobWrapper<BatchedConditionalImageCaptioningInput, kotlin.collections.List<ConditionalImageCaptioningOutput>>(
                startJobFunc = { inp ->
                    conditionalImageCaptioningApi.newBatchedJobApiTasksConditionalImageCaptioningBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    conditionalImageCaptioningApi.getBatchedJobResultsApiTasksConditionalImageCaptioningBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).map { it.caption }
    }

    fun faceEmbedding(image: BufferedImage): kotlin.collections.List<Float> {
        val input = FaceEmbeddingInput(image.toDataURL())
        val job = JobWrapper<FaceEmbeddingInput, FaceEmbeddingOutput>(
                startJobFunc = { inp ->
                    faceEmbeddingApi.newJobApiTasksFaceEmbeddingModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    faceEmbeddingApi.getJobResultsApiTasksFaceEmbeddingJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).embedding.map{it.toFloat()}
    }

    fun faceEmbedding(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<kotlin.collections.List<Float>> {
        val input = BatchedFaceEmbeddingInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedFaceEmbeddingInput, kotlin.collections.List<FaceEmbeddingOutput>>(
                startJobFunc = { inp ->
                    faceEmbeddingApi.newBatchedJobApiTasksFaceEmbeddingBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    faceEmbeddingApi.getBatchedJobResultsApiTasksFaceEmbeddingBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).map { it.embedding.map{it.toFloat()}}
    }

    fun objectDetection(image: BufferedImage): ObjectDetectionOutput {
        val input = ObjectDetectionInput(image.toDataURL())
        val job = JobWrapper<ObjectDetectionInput, ObjectDetectionOutput>(
                startJobFunc = { inp ->
                    objectDetectionApi.newJobApiTasksObjectDetectionModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    objectDetectionApi.getJobResultsApiTasksObjectDetectionJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input)
    }

    fun objectDetection(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<ObjectDetectionOutput> {
        val input = BatchedObjectDetectionInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedObjectDetectionInput, kotlin.collections.List<ObjectDetectionOutput>>(
                startJobFunc = { inp ->
                    objectDetectionApi.newBatchedJobApiTasksObjectDetectionBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    objectDetectionApi.getBatchedJobResultsApiTasksObjectDetectionBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input)
    }

    fun automatedSpeechRecognition(audio: AudioContent): String {
        val input = AutomatedSpeechRecognitionInput(audio.toDataURL())
        val job = JobWrapper<AutomatedSpeechRecognitionInput, AutomatedSpeechRecognitionOutput>(
                startJobFunc = { inp ->
                    automatedSpeechRecognitionApi.newJobApiTasksAutomatedSpeechRecognitionModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    automatedSpeechRecognitionApi.getJobResultsApiTasksAutomatedSpeechRecognitionJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).transcript
    }

    fun automatedSpeechRecognition(audio: kotlin.collections.List<AudioContent>): kotlin.collections.List<String> {
        val input = BatchedAutomatedSpeechRecognitionInput(audio.map { it.toDataURL() })
        val job = JobWrapper<BatchedAutomatedSpeechRecognitionInput, kotlin.collections.List<AutomatedSpeechRecognitionOutput>>(
                startJobFunc = { inp ->
                    automatedSpeechRecognitionApi.newBatchedJobApiTasksAutomatedSpeechRecognitionBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    automatedSpeechRecognitionApi.getBatchedJobResultsApiTasksAutomatedSpeechRecognitionBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).map { it.transcript }
    }

    fun opticalCharacterRecognition(image: BufferedImage): String {
        val input = OpticalCharacterRecognitionInput(image.toDataURL())
        val job = JobWrapper<OpticalCharacterRecognitionInput, OpticalCharacterRecognitionOutput>(
                startJobFunc = { inp ->
                    opticalCharacterRecognitionApi.newJobApiTasksOpticalCharacterRecognitionModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    opticalCharacterRecognitionApi.getJobResultsApiTasksOpticalCharacterRecognitionJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).text
    }

    fun opticalCharacterRecognition(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<String> {
        val input = BatchedOpticalCharacterRecognitionInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedOpticalCharacterRecognitionInput, kotlin.collections.List<OpticalCharacterRecognitionOutput>>(
                startJobFunc = { inp ->
                    opticalCharacterRecognitionApi.newBatchedJobApiTasksOpticalCharacterRecognitionBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    opticalCharacterRecognitionApi.getBatchedJobResultsApiTasksOpticalCharacterRecognitionBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                }
        )
        return job.executeJob(input).map { it.text }
    }
}