package org.vitrivr.engine.module.features.feature.external.implementations.prak

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Serializable
data class VideoQAMedia(
    val type: String = "frames",
    @SerialName("max_frames_per_video") val maxFramesPerVideo: Int = 16
)

@Serializable
data class VideoQAFilter(
    val days: List<String>,
    @SerialName("time_start") val timeStart: String? = null,
    @SerialName("time_end") val timeEnd: String? = null
)

@Serializable
data class VideoQARequest(
    val question: String,
    val media: VideoQAMedia,
    @SerialName("video_id") val videoId: List<String> = emptyList(),
    @SerialName("time_start") val timeStart: String? = null,
    @SerialName("time_end") val timeEnd: String? = null,
    @SerialName("camera_filter") val cameraFilter: List<String> = emptyList(),
    @SerialName("additional_filters") val additionalFilters: List<VideoQAFilter> = emptyList()
) {
    fun validate() {
        require(question.isNotBlank()) { "question must not be blank." }
        require(media.type in setOf("frames", "transcript")) { "Media type must be frames or transcript." }
        if (media.type == "frames") require(media.maxFramesPerVideo in 1..128) { "max_frames_per_video must be between 1 and 128." }
        require(videoId.isNotEmpty() || cameraFilter.isNotEmpty() || timeStart != null || timeEnd != null || additionalFilters.isNotEmpty()) {
            "At least one scope field must be provided."
        }
        require(videoId.all { it.matches(Regex("day[1-9][0-9]*_[A-Za-z0-9]+_([01][0-9]|2[0-3])")) }) { "Invalid video_id; expected dayN_Camera_HH." }
        if (videoId.isEmpty()) require(cameraFilter.all { it in cameras }) { "Unknown camera_filter." }
        fun hour(value: String): Pair<Int, Int> {
            require(value.matches(Regex("day[1-9][0-9]*:([01][0-9]|2[0-3])"))) { "Expected time in dayN:HH format." }
            return value.substringAfter("day").substringBefore(':').toInt() to value.substringAfter(':').toInt()
        }
        val start = timeStart?.let(::hour)
        val end = timeEnd?.let(::hour)
        if (start != null && end != null) require(end.first > start.first || (end.first == start.first && end.second > start.second)) { "time_end must follow time_start." }
        additionalFilters.forEach { filter ->
            require(filter.days.isNotEmpty() && filter.days.all { it.matches(Regex("day[1-9][0-9]*")) }) { "Filters require days in dayN format." }
            listOfNotNull(filter.timeStart, filter.timeEnd).forEach {
                require(it.matches(Regex("([01][0-9]|2[0-3]):[0-5][0-9]"))) { "Filter times must use HH:MM format." }
            }
            if (filter.timeStart != null && filter.timeEnd != null) require(filter.timeEnd > filter.timeStart) { "Filter time_end must follow time_start." }
        }
    }

    companion object {
        private val cameras = setOf("Allie", "Cathal", "Bjorn", "Florian", "Klaus", "Kitchen", "Luca", "Living", "Meeting", "Werner", "Tien", "Stevan")
    }
}

/** Makes no changes to stored data and performs no extraction. */
class VideoQAClient(private val endpoint: String) {
    fun answer(request: VideoQARequest): String {
        request.validate()
        val effective = if (request.videoId.isNotEmpty()) request.copy(cameraFilter = emptyList()) else request
        val payload = json.encodeToJsonElement(effective).jsonObject.let { encoded ->
            if (effective.media.type == "transcript") JsonObject(encoded + ("media" to buildJsonObject { put("type", "transcript") }))
            else encoded
        }
        val response = client.send(HttpRequest.newBuilder(URI.create(endpoint))
            .timeout(Duration.ofMinutes(3))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(payload))).build(), HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) { "QA service returned HTTP ${response.statusCode()}." }
        // The QA service wraps its answer in an object; retain string response compatibility.
        return if (response.headers().firstValue("Content-Type").orElse("").contains("application/json", ignoreCase = true)) {
            val value = try { json.parseToJsonElement(response.body()) } catch (e: IllegalArgumentException) {
                throw IllegalStateException("QA service returned invalid JSON.", e)
            }
            val answer = if (value is JsonObject) value["answer"] else value
            check(answer is JsonPrimitive && answer.isString) { "QA service did not return a string answer." }
            answer.content
        } else response.body()
    }

    companion object {
        private val json = Json { encodeDefaults = true; explicitNulls = false }
        private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
    }
}
