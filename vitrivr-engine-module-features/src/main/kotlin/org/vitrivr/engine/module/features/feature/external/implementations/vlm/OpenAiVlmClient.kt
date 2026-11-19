package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.source.file.MimeType
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ExecutionException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Sends image and video description requests to an OpenAI-compatible chat API.
 * The client can be shared across indexing workers and limits concurrent requests.
 *
 * @author Rahel Arnold
 */
class OpenAiVlmClient(
    baseUrl: String,
    private val model: String,
    private val prompt: String,
    private val promptVersion: String,
    private val maxOutputTokens: Int,
    private val timeout: Duration,
    maxConcurrentRequests: Int,
    private val disableThinking: Boolean = false
) : VlmService {
    private val endpoint = URI.create(baseUrl.trimEnd('/') + "/chat/completions")
    private val permits = Semaphore(maxConcurrentRequests, true)

    init {
        require(endpoint.scheme in setOf("http", "https") && endpoint.host != null &&
            endpoint.userInfo == null && endpoint.query == null && endpoint.fragment == null) {
            "VLM baseUrl must be an HTTP(S) API base URL."
        }
        require(model.isNotBlank() && prompt.isNotBlank() && promptVersion.isNotBlank())
        require(maxOutputTokens > 0 && timeout.toMillis() > 0 && maxConcurrentRequests > 0)
    }

    override fun describe(image: ImageContent): VlmDescription = describe(image.toBytes(), MimeType.PNG)

    fun describe(image: ByteArray, format: MimeType): VlmDescription {
        require(format in setOf(MimeType.PNG, MimeType.JPEG, MimeType.JPG)) { "VLM images must be JPEG or PNG." }
        return describeMedia(image, format, "image_url")
    }

    fun describeVideo(clip: ByteArray): VlmDescription = describeMedia(clip, MimeType.MP4, "video_url")

    private fun describeMedia(bytes: ByteArray, format: MimeType, partType: String): VlmDescription {
        require(bytes.isNotEmpty()) { "VLM media is empty." }
        val url = "data:${format.mimeType};base64,${Base64.getEncoder().encodeToString(bytes)}"
        val response = complete(listOf(
            buildJsonObject {
                put("type", "text")
                put("text", prompt)
            },
            buildJsonObject {
                put("type", partType)
                put(partType, buildJsonObject { put("url", url) })
            }
        ))
        val choice = (response["choices"] as? JsonArray)?.firstOrNull() as? JsonObject
        val message = choice?.get("message") as? JsonObject
        val text = message?.string("content")?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw VlmRequestException("VLM returned no description text.")
        val finishReason = choice.string("finish_reason")
        if (finishReason != null && finishReason != "stop") {
            throw VlmRequestException("VLM did not complete the description (finish reason: $finishReason).")
        }
        return VlmDescription(text, response.string("model") ?: model, promptVersion, buildJsonObject {
            put("requestedModel", model)
            put("generatedAt", Instant.now().toString())
            put("maxOutputTokens", maxOutputTokens)
            put("temperature", 0)
            put("disableThinking", disableThinking)
            if (partType == "video_url") put("inputMediaType", format.mimeType)
            response.string("id")?.let { put("responseId", it) }
            finishReason?.let { put("finishReason", it) }
            (response["usage"] as? JsonObject)?.let { put("usage", it) }
        })
    }

    private fun complete(parts: List<JsonObject>): JsonObject {
        val body = buildJsonObject {
            put("model", model)
            put("temperature", 0)
            put("max_tokens", maxOutputTokens)
            put("messages", JsonArray(listOf(buildJsonObject {
                put("role", "user")
                put("content", JsonArray(parts))
            })))
            if (disableThinking) {
                put("chat_template_kwargs", buildJsonObject { put("enable_thinking", false) })
            }
        }
        val request = HttpRequest.newBuilder(endpoint)
            .header("Content-Type", "application/json")
            .timeout(timeout)
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build()

        permits.acquire()
        val response = try {
            val pending = http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            try {
                pending.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
            } catch (e: TimeoutException) {
                pending.cancel(true)
                throw VlmRequestException("VLM request timed out after $timeout.", e)
            } catch (e: InterruptedException) {
                pending.cancel(true)
                throw e
            } catch (e: ExecutionException) {
                throw VlmRequestException("VLM request failed.", e.cause)
            }
        } finally {
            permits.release()
        }
        if (response.statusCode() !in 200..299) {
            throw VlmRequestException("VLM returned HTTP ${response.statusCode()}.")
        }
        return try {
            Json.parseToJsonElement(response.body()) as? JsonObject
                ?: throw VlmRequestException("VLM response must be a JSON object.")
        } catch (e: IllegalArgumentException) {
            throw VlmRequestException("VLM returned malformed JSON.", e)
        }
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

    private companion object {
        // vLLM's HTTP listener may reject a cleartext HTTP/2 upgrade on POST.
        val http: HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
    }
}
