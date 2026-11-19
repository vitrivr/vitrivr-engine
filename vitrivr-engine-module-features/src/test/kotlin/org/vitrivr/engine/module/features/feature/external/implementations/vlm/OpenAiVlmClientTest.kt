package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.source.file.MimeType
import java.awt.image.BufferedImage
import java.net.InetSocketAddress
import java.time.Duration
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO

class OpenAiVlmClientTest {
    private val image = InMemoryImageContent(BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB))

    @Test
    fun sendsImageAndPromptAndReadsGenerationMetadata() {
        val body = AtomicReference<String>()
        withServer(request = { body.set(it) }) { url ->
            val client = client(url)
            val png = client.describe(image)
            val jpeg = client.describe(image.toBytes(MimeType.JPEG), MimeType.JPEG)
            assertEquals(png.text, jpeg.text)
            assertEquals("A red bicycle.", png.text)
            assertEquals("served-model", png.model)
            assertEquals("test-v1", png.promptVersion)
            assertEquals("completion-123", png.generationMetadata.getValue("responseId").jsonPrimitive.content)
            val request = Json.parseToJsonElement(body.get()).jsonObject
            assertEquals("test-model", request.getValue("model").jsonPrimitive.content)
            assertEquals("0", request.getValue("temperature").jsonPrimitive.content)
            assertEquals("64", request.getValue("max_tokens").jsonPrimitive.content)
            val parts = request.getValue("messages").jsonArray.single().jsonObject.getValue("content").jsonArray
            assertEquals("Describe this image.", parts[0].jsonObject.getValue("text").jsonPrimitive.content)
            val imageUrl = parts[1].jsonObject.getValue("image_url").jsonObject.getValue("url").jsonPrimitive.content
            assertTrue(imageUrl.startsWith("data:image/jpeg;base64,"))
            assertEquals(image.width, ImageIO.read(Base64.getDecoder().decode(imageUrl.substringAfter(',')).inputStream()).width)
        }
    }

    @Test
    fun rejectsMissingBlankMalformedAndTruncatedDescriptions() {
        val failures = listOf(
            """{"choices":[]}""",
            """{"choices":[{"message":{"content":"  "}}]}""",
            """{"choices":[{"message":{"content":123}}]}""",
            """{"choices":[{"message":{"content":"A red"},"finish_reason":"length"}]}""",
            "{"
        )
        for (response in failures) {
            withServer(response = response) { url ->
                assertThrows(VlmRequestException::class.java) { client(url).describe(image) }
            }
        }
    }

    @Test
    fun handlesHttpErrorsAndUnreachableEndpoint() {
        val closedUrl = withServer(status = 503) { url ->
            val error = assertThrows(VlmRequestException::class.java) { client(url).describe(image) }
            assertTrue(error.message.orEmpty().contains("503"))
            url
        }
        assertThrows(VlmRequestException::class.java) { client(closedUrl).describe(image) }
    }

    @Test
    fun timesOutStalledResponses() {
        withServer(request = { Thread.sleep(300) }) { url ->
            assertThrows(VlmRequestException::class.java) {
                client(url, Duration.ofMillis(50)).describe(image)
            }
        }
    }

    private fun client(url: String, timeout: Duration = Duration.ofSeconds(3)) =
        OpenAiVlmClient(url, "test-model", "Describe this image.", "test-v1", 64, timeout, 2)

    private fun <T> withServer(
        response: String = """{"id":"completion-123","model":"served-model","choices":[{"message":{"content":"  A red bicycle. "},"finish_reason":"stop"}],"usage":{"completion_tokens":5}}""",
        status: Int = 200,
        request: (String) -> Unit = {},
        run: (String) -> T
    ): T {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v1/chat/completions") { exchange ->
            exchange.use {
                request(it.requestBody.bufferedReader().use { reader -> reader.readText() })
                val bytes = response.toByteArray()
                it.sendResponseHeaders(status, bytes.size.toLong())
                it.responseBody.write(bytes)
            }
        }
        server.start()
        return try {
            run("http://127.0.0.1:${server.address.port}/v1/")
        } finally {
            server.stop(0)
        }
    }
}
