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

/*
    * Wrapper class for the external API.
    * @param hostName The hostname of the API.
    * @param model The model to use for the API.
    * @param timeoutSeconds The timeout in seconds for the API calls.
 */
class ApiWrapper(private val hostName:String, private val model: String, private val timeoutSeconds: Long, private val pollingIntervalMs: Long, private val retries: Int) {

    private val okHttpClient = OkHttpClient().newBuilder()
        .readTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS).connectTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS).writeTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS).callTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
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

    private fun <T, S> executeJob(taskName: String, inp: T, startJobFunc: (T) -> JobStatus, getJobResultFunc: (String) -> JobResult<S>): S {
        var retries = retries
        while (retries > 0) {
            try {
                val jobStatus: JobStatus
                try{
                    jobStatus = startJobFunc(inp)
                } catch (e: SocketTimeoutException) {
                    logger.error { "Failed to start $model $taskName Job on Host $hostName: API call timed out." }
                    throw e
                }
                var jobResult = getJobResultFunc(jobStatus.id)

                while (jobResult.status != JobState.complete) {
                    if (jobResult.status == JobState.failed) {
                        logger.error{"$model $taskName Job on Host $hostName with ID: ${jobStatus.id} failed."}
                        throw Exception("Job failed.")
                    }
                    logger.debug{"Waiting for $model $taskName job completion on Host $hostName with ID ${jobStatus.id}. Current status: ${jobResult.status}"}
                    Thread.sleep(this.pollingIntervalMs)
                    jobResult = getJobResultFunc(jobStatus.id)
                }

                return jobResult.result ?: run {
                    logger.error{"$model $taskName Job on Host $hostName with ID: ${jobStatus.id} returned no result."}
                    throw Exception("$model $taskName Job on Host $hostName with ID: ${jobStatus.id} returned no result.")
                }
            } catch (e: Exception) {
                logger.error { "$model $taskName Job on Host $hostName failed. Retrying..."}
                retries -= 1
            }
            Thread.sleep(this.pollingIntervalMs)
        }
        throw Exception("$model $taskName Job failed after $retries retries.")
    }

    /*
    * Method to get the text embedding for a given text.
    * @param text The text for which to get the embedding.
    * @return The embedding for the text.
     */
    fun textEmbedding(text: String): kotlin.collections.List<Float> {
        logger.info{ "Starting text embedding for text: \"$text\"" }
        val input = TextEmbeddingInput(text)

        return executeJob(
            taskName = "Text Embedding",
            inp = input,
            startJobFunc = { inp -> textEmbeddingApi.newJobApiTasksTextEmbeddingModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> textEmbeddingApi.getJobResultsApiTasksTextEmbeddingJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).embedding.map{it.toFloat()}.also{
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

        return executeJob(
            taskName = "Batched Text Embedding",
            inp = input,
            startJobFunc = { inp -> textEmbeddingApi.newBatchedJobApiTasksTextEmbeddingBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> textEmbeddingApi.getBatchedJobResultsApiTasksTextEmbeddingBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.embedding.map{it.toFloat()} }.also {
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

        return executeJob(
            taskName = "Image Embedding",
            inp = input,
            startJobFunc = { inp -> imageEmbeddingApi.newJobApiTasksImageEmbeddingModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> imageEmbeddingApi.getJobResultsApiTasksImageEmbeddingJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).embedding.map{it.toFloat()}.also {
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
        return executeJob(
            taskName = "Batched Image Embedding",
            inp = input,
            startJobFunc = { inp -> imageEmbeddingApi.newBatchedJobApiTasksImageEmbeddingBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> imageEmbeddingApi.getBatchedJobResultsApiTasksImageEmbeddingBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.embedding.map{it.toFloat()}}.also {
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
        return executeJob(
            taskName = "Image Captioning",
            inp = input,
            startJobFunc = { inp -> imageCaptioningApi.newJobApiTasksImageCaptioningModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> imageCaptioningApi.getJobResultsApiTasksImageCaptioningJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).caption.also {
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

        return executeJob(
            taskName = "Batched Image Captioning",
            inp = input,
            startJobFunc = { inp -> imageCaptioningApi.newBatchedJobApiTasksImageCaptioningBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> imageCaptioningApi.getBatchedJobResultsApiTasksImageCaptioningBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.caption }.also {
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
        return executeJob(
            taskName = "Zero Shot Image Classification",
            inp = input,
            startJobFunc = { inp -> zeroShotImageClassificationApi.newJobApiTasksZeroShotImageClassificationModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> zeroShotImageClassificationApi.getJobResultsApiTasksZeroShotImageClassificationJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).probabilities.map{it.toFloat()}.also {
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
        return executeJob(
            taskName = "Batched Zero Shot Image Classification",
            inp = input,
            startJobFunc = { inp -> zeroShotImageClassificationApi.newBatchedJobApiTasksZeroShotImageClassificationBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> zeroShotImageClassificationApi.getBatchedJobResultsApiTasksZeroShotImageClassificationBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.probabilities.map{it.toFloat()}}.also {
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
        return executeJob(
            taskName = "Conditional Image Captioning",
            inp = input,
            startJobFunc = { inp -> conditionalImageCaptioningApi.newJobApiTasksConditionalImageCaptioningModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> conditionalImageCaptioningApi.getJobResultsApiTasksConditionalImageCaptioningJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).caption.also {
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

        return executeJob(
            taskName = "Batched Conditional Image Captioning",
            inp = input,
            startJobFunc = { inp -> conditionalImageCaptioningApi.newBatchedJobApiTasksConditionalImageCaptioningBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> conditionalImageCaptioningApi.getBatchedJobResultsApiTasksConditionalImageCaptioningBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.caption }.also {
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
        return executeJob(
            taskName = "Face Embedding",
            inp = input,
            startJobFunc = { inp -> faceEmbeddingApi.newJobApiTasksFaceEmbeddingModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> faceEmbeddingApi.getJobResultsApiTasksFaceEmbeddingJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).embedding.map{it.toFloat()}.also {
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

        return executeJob(
            taskName = "Batched Face Embedding",
            inp = input,
            startJobFunc = { inp -> faceEmbeddingApi.newBatchedJobApiTasksFaceEmbeddingBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> faceEmbeddingApi.getBatchedJobResultsApiTasksFaceEmbeddingBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.embedding.map{it.toFloat()}}.also {
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

        return executeJob(
            taskName = "Object Detection",
            inp = input,
            startJobFunc = { inp -> objectDetectionApi.newJobApiTasksObjectDetectionModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> objectDetectionApi.getJobResultsApiTasksObjectDetectionJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).also {
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
        return executeJob(
            taskName = "Batched Object Detection",
            inp = input,
            startJobFunc = { inp -> objectDetectionApi.newBatchedJobApiTasksObjectDetectionBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> objectDetectionApi.getBatchedJobResultsApiTasksObjectDetectionBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).also {
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

        return executeJob(
            taskName = "Automated Speech Recognition",
            inp = input,
            startJobFunc = { inp -> automatedSpeechRecognitionApi.newJobApiTasksAutomatedSpeechRecognitionModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> automatedSpeechRecognitionApi.getJobResultsApiTasksAutomatedSpeechRecognitionJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).transcript.also {
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

        return executeJob(
            taskName = "Batched Automated Speech Recognition",
            inp = input,
            startJobFunc = { inp -> automatedSpeechRecognitionApi.newBatchedJobApiTasksAutomatedSpeechRecognitionBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> automatedSpeechRecognitionApi.getBatchedJobResultsApiTasksAutomatedSpeechRecognitionBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.transcript }.also {
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

        return executeJob(
            taskName = "Optical Character Recognition",
            inp = input,
            startJobFunc = { inp -> opticalCharacterRecognitionApi.newJobApiTasksOpticalCharacterRecognitionModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> opticalCharacterRecognitionApi.getJobResultsApiTasksOpticalCharacterRecognitionJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).text.also {
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

        return executeJob(
            taskName = "Batched Optical Character Recognition",
            inp = input,
            startJobFunc = { inp -> opticalCharacterRecognitionApi.newBatchedJobApiTasksOpticalCharacterRecognitionBatchedModelJobsPost(model, inp) },
            getJobResultFunc = { jobId -> opticalCharacterRecognitionApi.getBatchedJobResultsApiTasksOpticalCharacterRecognitionBatchedJobsJobGet(jobId).let { JobResult(it.status, it.result) } }
        ).map { it.text }.also {
            logger.info{ "Batched optical character recognition result: $it" }
        }
    }

//    /*
//    * Method to get the text query embedding for a given text.
//    * @param query The text for which to get the embedding.
//    * @param instruction The instruction detailing the retrieval task.
//    * @return The embedding for the text.
//     */
//    fun textQueryEmbedding(query: String, instruction: String): kotlin.collections.List<Float> {
//        logger.info{ "Starting text query embedding for query: \"$query\"" }
//        val input = TextQueryEmbeddingInput(query, instruction)
//
//        return executeJob(
//            taskName = "Text Query Embedding",
//            inp = input,
//            startJobFunc = { inp -> textQueryEmbeddingApi.newJobApiTasksTextQueryEmbeddingModelJobsPost(model, inp).body() },
//            getJobResultFunc = { jobId -> textQueryEmbeddingApi.getJobResultsApiTasksTextQueryEmbeddingJobsJobGet(jobId).body().let { JobResult(it.status, it.result) } }
//        ).embedding.map{it.toFloat()}.also {
//            logger.info{ "Text query embedding result: $it" }
//        }
//    }
//
//    /*
//    * Method to get the text query embedding for a list of texts.
//    * @param query The list of texts for which to get the embedding.
//    * @param instruction The instruction detailing the retrieval task.
//     */
//    fun textQueryEmbedding(query: kotlin.collections.List<String>, instruction: String): kotlin.collections.List<kotlin.collections.List<Float>> {
//        logger.info{ "Starting batched text query embedding for queries: \"$query\" (batch size: ${query.size})" }
//        val input = BatchedTextQueryEmbeddingInput(query, instruction)
//
//        return executeJob(
//            taskName = "Batched Text Query Embedding",
//            inp = input,
//            startJobFunc = { inp -> textQueryEmbeddingApi.newBatchedJobApiTasksTextQueryEmbeddingBatchedModelJobsPost(model, inp).body() },
//            getJobResultFunc = { jobId -> textQueryEmbeddingApi.getBatchedJobResultsApiTasksTextQueryEmbeddingBatchedJobsJobGet(jobId).body().let { JobResult(it.status, it.result) } }
//        ).map { it.embedding.map{it.toFloat()}}.also {
//            logger.info{ "Batched text query embedding result: $it" }
//        }
//    }
}