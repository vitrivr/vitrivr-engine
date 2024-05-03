package org.vitrivr.engine.base.features.external.common

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import org.openapitools.client.apis.*
import org.openapitools.client.models.*
import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.util.extension.toDataURL
import java.awt.image.BufferedImage
import java.net.SocketTimeoutException


private val logger: KLogger = KotlinLogging.logger {}

internal data class JobResult<S>(
        val status: JobState,
        val result: S?
)

internal class JobWrapper<T, S>(
    private val startJobFunc: (T) -> JobStatus,
    private val getJobResultFunc: (String) -> JobResult<S>,
    private val pollingIntervalMs: Long
){

    fun executeJob(inp: T): S {
        val jobStatus: JobStatus
        try{
            jobStatus = startJobFunc(inp)
        } catch (e: SocketTimeoutException) {
            logger.error { "Failed to start Job: API call timed out." }
            throw e
        }
        var jobResult = getJobResultFunc(jobStatus.id)

        while (jobResult.status != JobState.complete) {
            if (jobResult.status == JobState.failed) {
                logger.error{"Job with ID: ${jobStatus.id} failed."}
                throw Exception("Job failed.")
            }
            logger.debug{"Waiting for job completion. Current status: ${jobResult.status}"}
            Thread.sleep(this.pollingIntervalMs)
            jobResult = getJobResultFunc(jobStatus.id)
        }

        return jobResult.result ?: run {
            logger.error{"Job with ID: ${jobStatus.id} returned no result."}
            throw Exception("Job returned no result.")
        }
    }
}

/*
    * Wrapper class for the external API.
    * @param hostName The hostname of the API.
    * @param model The model to use for the API.
    * @param timeoutSeconds The timeout in seconds for the API calls.
 */
class ApiWrapper(private val hostName:String, private val model: String, private val timeoutSeconds: Long, private val pollingIntervalMs: Long ) {

    private val okHttpClient = OkHttpClient().newBuilder()
        .readTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val textEmbeddingApi = TextEmbeddingApi(basePath = hostName, client = okHttpClient)
    private val imageEmbeddingApi = ImageEmbeddingApi(basePath = hostName, client = okHttpClient)
    private val imageCaptioningApi = ImageCaptioningApi(basePath = hostName, client = okHttpClient)
    private val zeroShotImageClassificationApi = ZeroShotImageClassificationApi(basePath = hostName, client = okHttpClient)
    private val conditionalImageCaptioningApi = ConditionalImageCaptioningApi(basePath = hostName, client = okHttpClient)
    private val faceEmbeddingApi = FaceEmbeddingApi(basePath = hostName, client = okHttpClient)
    private val objectDetectionApi = ObjectDetectionApi(basePath = hostName, client = okHttpClient)
    private val automatedSpeechRecognitionApi = AutomatedSpeechRecognitionApi(basePath = hostName, client = okHttpClient)
    private val opticalCharacterRecognitionApi = OpticalCharacterRecognitionApi(basePath = hostName, client = okHttpClient)

    init {
        logger.info{ "Initialized API wrapper with host: $hostName, model: $model, timeout: $timeoutSeconds seconds, polling interval: $pollingIntervalMs ms" }
    }

    /*
    * Method to get the text embedding for a given text.
    * @param text The text for which to get the embedding.
    * @return The embedding for the text.
     */
    fun textEmbedding(text: String): kotlin.collections.List<Float> {
        logger.info{ "Starting text embedding for text: \"$text\"" }
        val input = TextEmbeddingInput(text)
        val job = JobWrapper<TextEmbeddingInput, TextEmbeddingOutput>(
                startJobFunc = { inp ->
                    textEmbeddingApi.newJobApiTasksTextEmbeddingModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    textEmbeddingApi.getJobResultsApiTasksTextEmbeddingJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )

        return job.executeJob(input).embedding.map{it.toFloat()}.also{
            logger.info{ "Text embedding result: $it" }
        }
    }

    /*
    * Method to get the text embedding for a list of texts.
    * @param text The list of texts for which to get the embedding.
    * @return The embedding for the texts.
     */
    fun textEmbedding(text: kotlin.collections.List<String>): kotlin.collections.List<kotlin.collections.List<Float>> {
        logger.info{ "Starting batched text embedding for texts: \"$text\" (batch size: ${text.size} )" }
        val input = BatchedTextEmbeddingInput(text)
        val job = JobWrapper<BatchedTextEmbeddingInput, kotlin.collections.List<TextEmbeddingOutput>>(
                startJobFunc = { inp ->
                    textEmbeddingApi.newBatchedJobApiTasksTextEmbeddingBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    textEmbeddingApi.getBatchedJobResultsApiTasksTextEmbeddingBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.embedding.map{it.toFloat()} }.also {
            logger.info{ "Batched text embedding result: $it" }
        }
    }

    /*
    * Method to get the image embedding for a given image.
    * @param image The image for which to get the embedding.
    * @return The embedding for the image.
     */
    fun imageEmbedding(image: BufferedImage): kotlin.collections.List<Float> {
        logger.info{ "Starting image embedding for image: \"$image\"" }
        val input = ImageEmbeddingInput(image.toDataURL())
        val job = JobWrapper<ImageEmbeddingInput, ImageEmbeddingOutput>(
                startJobFunc = { inp ->
                    imageEmbeddingApi.newJobApiTasksImageEmbeddingModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageEmbeddingApi.getJobResultsApiTasksImageEmbeddingJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).embedding.map{it.toFloat()}.also {
            logger.info{ "Image embedding result: $it" }
        }
    }

    /*
    * Method to get the image embedding for a list of images.
    * @param image The list of images for which to get the embedding.
    * @return The embedding for the images.
     */
    fun imageEmbedding(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<kotlin.collections.List<Float>> {
        logger.info{ "Starting batched image embedding for images: \"$image\" (batch size: ${image.size})" }
        val input = BatchedImageEmbeddingInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedImageEmbeddingInput, kotlin.collections.List<ImageEmbeddingOutput>>(
                startJobFunc = { inp ->
                    imageEmbeddingApi.newBatchedJobApiTasksImageEmbeddingBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageEmbeddingApi.getBatchedJobResultsApiTasksImageEmbeddingBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.embedding.map{it.toFloat()}}.also {
            logger.info{ "Batched image embedding result: $it" }
        }
    }

    /*
    * Method to get the image captioning for a given image.
    * @param image The image for which to get the caption.
    * @return The caption for the image.
     */
    fun imageCaptioning(image: BufferedImage): String {
        logger.info{ "Starting image captioning for image: \"$image\"" }
        val input = ImageCaptioningInput(image.toDataURL())
        val job = JobWrapper<ImageCaptioningInput, ImageCaptioningOutput>(
                startJobFunc = { inp ->
                    imageCaptioningApi.newJobApiTasksImageCaptioningModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageCaptioningApi.getJobResultsApiTasksImageCaptioningJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).caption.also {
            logger.info{ "Image captioning result: $it" }
        }
    }

    /*
    * Method to get the image captioning for a list of images.
    * @param image The list of images for which to get the caption.
    * @return The caption for the images.
     */
    fun imageCaptioning(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<String> {
        logger.info{ "Starting batched image captioning for images: \"$image\" (batch size: ${image.size})" }
        val input = BatchedImageCaptioningInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedImageCaptioningInput, kotlin.collections.List<ImageCaptioningOutput>>(
                startJobFunc = { inp ->
                    imageCaptioningApi.newBatchedJobApiTasksImageCaptioningBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    imageCaptioningApi.getBatchedJobResultsApiTasksImageCaptioningBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.caption }.also {
            logger.info{ "Batched image captioning result: $it" }
        }
    }

    /*
    * Method to get the zero shot image classification for a given image.
    * @param image The image for which to get the classification.
    * @param labels The list of labels to classify the image.
    * @return The classification probabilities for the image.
     */
    fun zeroShotImageClassification(image: BufferedImage, labels: kotlin.collections.List<String>): kotlin.collections.List<Float> {
        logger.info{ "Starting zero shot image classification for image: \"$image\"" }
        val input = ZeroShotImageClassificationInput(image.toDataURL(), labels)
        val job = JobWrapper<ZeroShotImageClassificationInput, ZeroShotImageClassificationOutput>(
                startJobFunc = { inp ->
                    zeroShotImageClassificationApi.newJobApiTasksZeroShotImageClassificationModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    zeroShotImageClassificationApi.getJobResultsApiTasksZeroShotImageClassificationJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).probabilities.map{it.toFloat()}.also {
            logger.info{ "Zero shot image classification result: $it" }
        }
    }

    /*
    * Method to get the zero shot image classification for a list of images.
    * @param image The list of images for which to get the classification.
    * @param labels The list of labels to classify the images.
    * @return The classification probabilities for the images.
     */
    fun zeroShotImageClassification(image: kotlin.collections.List<BufferedImage>, labels: kotlin.collections.List<String>): kotlin.collections.List<kotlin.collections.List<Float>> {
        logger.info{ "Starting batched zero shot image classification for images: \"$image\" (batch size: ${image.size})" }
        val input = BatchedZeroShotImageClassificationInput(image.map { it.toDataURL() }, labels)
        val job = JobWrapper<BatchedZeroShotImageClassificationInput, kotlin.collections.List<ZeroShotImageClassificationOutput>>(
                startJobFunc = { inp ->
                    zeroShotImageClassificationApi.newBatchedJobApiTasksZeroShotImageClassificationBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    zeroShotImageClassificationApi.getBatchedJobResultsApiTasksZeroShotImageClassificationBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.probabilities.map{it.toFloat()}}.also {
            logger.info{ "Batched zero shot image classification result: $it" }
        }
    }

    /*
    * Method to get the conditional image captioning for a given image.
    * @param image The image for which to get the caption.
    * @param text The text to condition the caption on.
    * @return The caption for the image.
     */
    fun conditionalImageCaptioning(image: BufferedImage, text: String): String {
        logger.info{ "Starting conditional image captioning for image: \"$image\"" }
        val input = ConditionalImageCaptioningInput(image.toDataURL(), text)
        val job = JobWrapper<ConditionalImageCaptioningInput, ConditionalImageCaptioningOutput>(
                startJobFunc = { inp ->
                    conditionalImageCaptioningApi.newJobApiTasksConditionalImageCaptioningModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    conditionalImageCaptioningApi.getJobResultsApiTasksConditionalImageCaptioningJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).caption.also {
            logger.info{ "Conditional image captioning result: $it" }
        }
    }

    /*
    * Method to get the conditional image captioning for a list of images.
    * @param image The list of images for which to get the caption.
    * @param text The list of texts to condition the caption on.
    * @return The caption for the images.
     */
    fun conditionalImageCaptioning(image: kotlin.collections.List<BufferedImage>, text: kotlin.collections.List<String>): kotlin.collections.List<String> {
        logger.info{ "Starting batched conditional image captioning for images: \"$image\" (batch size: ${image.size})" }
        val input = BatchedConditionalImageCaptioningInput(image.map { it.toDataURL() }, text)
        val job = JobWrapper<BatchedConditionalImageCaptioningInput, kotlin.collections.List<ConditionalImageCaptioningOutput>>(
                startJobFunc = { inp ->
                    conditionalImageCaptioningApi.newBatchedJobApiTasksConditionalImageCaptioningBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    conditionalImageCaptioningApi.getBatchedJobResultsApiTasksConditionalImageCaptioningBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.caption }.also {
            logger.info{ "Batched conditional image captioning result: $it" }
        }
    }

    /*
    * Method to get the face embedding for a given image.
    * @param image The image for which to get the embedding.
    * @return The embedding for the image.
     */
    fun faceEmbedding(image: BufferedImage): kotlin.collections.List<Float> {
        logger.info{ "Starting face embedding for image: \"$image\"" }
        val input = FaceEmbeddingInput(image.toDataURL())
        val job = JobWrapper<FaceEmbeddingInput, FaceEmbeddingOutput>(
                startJobFunc = { inp ->
                    faceEmbeddingApi.newJobApiTasksFaceEmbeddingModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    faceEmbeddingApi.getJobResultsApiTasksFaceEmbeddingJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).embedding.map{it.toFloat()}.also {
            logger.info{ "Face embedding result: $it" }
        }
    }

    /*
    * Method to get the face embedding for a list of images.
    * @param image The list of images for which to get the embedding.
    * @return The embedding for the images.
     */
    fun faceEmbedding(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<kotlin.collections.List<Float>> {
        logger.info{ "Starting batched face embedding for images: \"$image\" (batch size: ${image.size})" }
        val input = BatchedFaceEmbeddingInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedFaceEmbeddingInput, kotlin.collections.List<FaceEmbeddingOutput>>(
                startJobFunc = { inp ->
                    faceEmbeddingApi.newBatchedJobApiTasksFaceEmbeddingBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    faceEmbeddingApi.getBatchedJobResultsApiTasksFaceEmbeddingBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.embedding.map{it.toFloat()}}.also {
            logger.info{ "Batched face embedding result: $it" }
        }
    }

    /*
    * Method to get the object detection for a given image.
    * @param image The image for which to get the object detection.
    * @return The object detection for the image.
     */
    fun objectDetection(image: BufferedImage): ObjectDetectionOutput {
        logger.info{ "Starting object detection for image: \"$image\"" }
        val input = ObjectDetectionInput(image.toDataURL())
        val job = JobWrapper<ObjectDetectionInput, ObjectDetectionOutput>(
                startJobFunc = { inp ->
                    objectDetectionApi.newJobApiTasksObjectDetectionModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    objectDetectionApi.getJobResultsApiTasksObjectDetectionJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).also {
            logger.info{ "Object detection result: $it" }
        }
    }

    /*
    * Method to get the object detection for a list of images.
    * @param image The list of images for which to get the object detection.
    * @return The object detection for the images.
     */
    fun objectDetection(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<ObjectDetectionOutput> {
        logger.info{ "Starting batched object detection for images: \"$image\" (batch size: ${image.size})" }
        val input = BatchedObjectDetectionInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedObjectDetectionInput, kotlin.collections.List<ObjectDetectionOutput>>(
                startJobFunc = { inp ->
                    objectDetectionApi.newBatchedJobApiTasksObjectDetectionBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    objectDetectionApi.getBatchedJobResultsApiTasksObjectDetectionBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).also {
            logger.info{ "Batched object detection result: $it" }
        }
    }

    /*
    * Method to get the automated speech recognition for a given audio.
    * @param audio The audio for which to get the transcript.
    * @return The transcript for the audio.
     */
    fun automatedSpeechRecognition(audio: AudioContent): String {
        logger.info{ "Starting automated speech recognition for audio: \"$audio\"" }
        val input = AutomatedSpeechRecognitionInput(audio.toDataURL())
        val job = JobWrapper<AutomatedSpeechRecognitionInput, AutomatedSpeechRecognitionOutput>(
                startJobFunc = { inp ->
                    automatedSpeechRecognitionApi.newJobApiTasksAutomatedSpeechRecognitionModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    automatedSpeechRecognitionApi.getJobResultsApiTasksAutomatedSpeechRecognitionJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).transcript.also {
            logger.info{ "Automated speech recognition result: $it" }
        }
    }

    /*
    * Method to get the automated speech recognition for a list of audio.
    * @param audio The list of audio for which to get the transcript.
    * @return The transcript for the audio.
     */
    fun automatedSpeechRecognition(audio: kotlin.collections.List<AudioContent>): kotlin.collections.List<String> {
        logger.info{ "Starting batched automated speech recognition for audio: \"$audio\" (batch size: ${audio.size} )" }
        val input = BatchedAutomatedSpeechRecognitionInput(audio.map { it.toDataURL() })
        val job = JobWrapper<BatchedAutomatedSpeechRecognitionInput, kotlin.collections.List<AutomatedSpeechRecognitionOutput>>(
                startJobFunc = { inp ->
                    automatedSpeechRecognitionApi.newBatchedJobApiTasksAutomatedSpeechRecognitionBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    automatedSpeechRecognitionApi.getBatchedJobResultsApiTasksAutomatedSpeechRecognitionBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.transcript }.also {
            logger.info{ "Batched automated speech recognition result: $it" }
        }
    }

    /*
    * Method to get the optical character recognition for a given image.
    * @param image The image for which to get the text.
    * @return The text for the image.
     */
    fun opticalCharacterRecognition(image: BufferedImage): String {
        logger.info{ "Starting optical character recognition for image: \"$image\"" }
        val input = OpticalCharacterRecognitionInput(image.toDataURL()).also {
            logger.info{ "Optical character recognition input: $it" }
        }
        val job = JobWrapper<OpticalCharacterRecognitionInput, OpticalCharacterRecognitionOutput>(
                startJobFunc = { inp ->
                    opticalCharacterRecognitionApi.newJobApiTasksOpticalCharacterRecognitionModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    opticalCharacterRecognitionApi.getJobResultsApiTasksOpticalCharacterRecognitionJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).text.also {
            logger.info{ "Optical character recognition result: $it" }
        }
    }

    /*
    * Method to get the optical character recognition for a list of images.
    * @param image The list of images for which to get the text.
    * @return The text for the images.
     */
    fun opticalCharacterRecognition(image: kotlin.collections.List<BufferedImage>): kotlin.collections.List<String> {
        logger.info{ "Starting batched optical character recognition for images: \"$image\" (batch size: ${image.size})" }
        val input = BatchedOpticalCharacterRecognitionInput(image.map { it.toDataURL() })
        val job = JobWrapper<BatchedOpticalCharacterRecognitionInput, kotlin.collections.List<OpticalCharacterRecognitionOutput>>(
                startJobFunc = { inp ->
                    opticalCharacterRecognitionApi.newBatchedJobApiTasksOpticalCharacterRecognitionBatchedModelJobsPost(model, inp)
                },
                getJobResultFunc = { jobId ->
                    opticalCharacterRecognitionApi.getBatchedJobResultsApiTasksOpticalCharacterRecognitionBatchedJobsJobGet(jobId).let {
                        JobResult(it.status, it.result)
                    }
                },
                pollingIntervalMs = pollingIntervalMs
        )
        return job.executeJob(input).map { it.text }.also {
            logger.info{ "Batched optical character recognition result: $it" }
        }
    }
}