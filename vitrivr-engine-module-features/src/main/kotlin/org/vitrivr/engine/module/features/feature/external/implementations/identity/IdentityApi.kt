package org.vitrivr.engine.module.features.feature.external.implementations.identity

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.model.descriptor.struct.FaceIdentityDescriptor
import org.vitrivr.engine.module.features.feature.external.ExternalAnalyser
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/** HTTP and response mapping shared by the person-identification analysers. */
internal object IdentityApi {
    private val json = Json { ignoreUnknownKeys = true }

    fun post(host: String, path: String, parameters: Map<String, String>): JsonObject = runBlocking {
        val body = parameters.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        val url = "${host.trimEnd('/')}$path"
        val response = ExternalAnalyser.httpClient.post(url) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(body)
        }
        val responseBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            val detail = runCatching {
                json.parseToJsonElement(responseBody).jsonObject["error"]?.jsonPrimitive?.content
            }.getOrNull() ?: responseBody.take(500)
            throw IllegalArgumentException(
                "Person-identification service returned ${response.status.value} from $url: $detail"
            )
        }
        json.parseToJsonElement(responseBody).jsonObject
    }

    fun imageParameters(image: ImageContent): Map<String, String> = mapOf("data" to image.toDataUrl())

    fun labels(persons: JsonArray): List<FaceIdentityDescriptor> = persons.mapNotNull { element ->
        val person = element.jsonObject
        val name = person["person"]?.jsonPrimitive?.contentOrNull
        val face = person["face"] as? JsonObject
        val embedding = (person["embedding"] ?: face?.get("embedding")) as? JsonArray
        val vector = embedding?.let { Value.FloatVector(it.map { value -> value.jsonPrimitive.float }.toFloatArray()) }
        if (name == null && vector == null) return@mapNotNull null
        val score = sequenceOf("face_score", "body_score", "score", "detection_score")
            .mapNotNull { person[it]?.jsonPrimitive?.floatOrNull }
            .firstOrNull() ?: 1f
        FaceIdentityDescriptor(UUID.randomUUID(), null, name ?: "unknown", score, vector)
    }

    fun faces(response: JsonObject): List<FaceIdentityDescriptor> =
        labels(response["faces"]?.jsonArray ?: JsonArray(emptyList()))

    fun persons(response: JsonObject): List<FaceIdentityDescriptor> =
        labels(response["persons"]?.jsonArray ?: JsonArray(emptyList()))

    fun finalizedFrames(response: JsonObject): Map<Int, List<FaceIdentityDescriptor>> =
        response["frames"]?.jsonArray.orEmpty().associate { frameElement ->
            val frame = frameElement.jsonObject
            val frameIndex = frame["frame_index"]?.jsonPrimitive?.int
                ?: throw IllegalArgumentException("Finalized frame has no frame_index.")
            frameIndex to labels(frame["persons"]?.jsonArray ?: JsonArray(emptyList()))
        }

    private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
