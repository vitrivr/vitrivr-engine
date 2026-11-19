package org.vitrivr.engine.database.pgvector.descriptor

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import org.vitrivr.engine.core.config.ingest.IngestionConfig
import org.vitrivr.engine.core.config.schema.SchemaConfig
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.factory.InMemoryContentFactory
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.result.QueryResult
import org.vitrivr.engine.query.parsing.QueryParser
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

class VlmCaptionIntegrationTest {
    @TempDir
    lateinit var keyframes: Path

    @Test
    fun captionsAreUpsertedAndRankedWithOriginalShotMetadata() = runBlocking {
        val requests = AtomicInteger()
        val failure = AtomicInteger(-1)
        val server = mockVlm(requests, failure)
        try {
            val schema = loadSchema("http://127.0.0.1:${server.address.port}/v1")
            val shots = listOf(
                seedShot(schema, Color.RED, 10, 20),
                seedShot(schema, Color.GREEN, 30, 40),
                seedShot(schema, Color.BLUE, 50, 60)
            )
            val pipeline = requireNotNull(schema.getIngestionPipelineBuilder("captions")).build().single()
            try {
                pipeline.toFlow(this).toList()
                val reader = requireNotNull(schema["vlmcaption"]).getReader()
                assertEquals(3, requests.get())
                assertEquals(3L, reader.count())
                val original = reader.getForRetrievable(shots[0]).single()
                val refreshed = reader.getForRetrievable(shots[1]).single()
                assertEquals(Value.Text("mock-served-model"), original.values()["model"])
                assertEquals(Value.Text("1"), original.values()["prompt_version"])
                assertTrue(original.values()["generation_metadata"]!!.value.toString().contains("mock-completion"))

                // A retry writes to the same descriptor IDs. A failed refresh keeps the previous caption.
                failure.set(Color.RED.rgb)
                pipeline.toFlow(this).toList()
                assertEquals(6, requests.get())
                assertEquals(3L, reader.count())
                assertEquals(original.values(), reader.get(original.id)?.values())
                assertEquals(original.id, reader.getForRetrievable(shots[0]).single().id)
                val updated = reader.getForRetrievable(shots[1]).single()
                assertEquals(refreshed.id, updated.id)
                assertTrue(refreshed.values()["generation_metadata"] != updated.values()["generation_metadata"])

                server.stop(0)
                val result = QueryResult(QueryParser(schema).parse(exampleQuery()).toFlow(this).toList())
                val results = result.retrievables
                assertTrue(Json.encodeToString(result).contains("vlmcaption.caption"))
                assertEquals(shots.take(2).map(UUID::toString), results.map { it.id })
                assertTrue(results[0].score > results[1].score && results[1].score > 0)
                assertEquals(Value.Long(10), results[0].descriptors["time.start"])
                assertEquals(Value.Long(20), results[0].descriptors["time.end"])
                assertFalse(results.any { it.id == shots[2].toString() })
                assertEquals(6, requests.get())

                // The existing CLIP and SigLIP 2 fields still use their own dense descriptors.
                val context = Context(schema, InMemoryContentFactory().newContentFactory(schema, emptyMap()))
                for ((fieldName, dimension) in listOf("clip" to 512, "siglip2" to 768)) {
                    val field = requireNotNull(schema[fieldName])
                    val vector = FloatArray(dimension).apply { this[0] = 1.0f }
                    val descriptor = FloatVectorDescriptor(UUID.randomUUID(), shots[2], Value.FloatVector(vector))
                    assertTrue(field.getWriter().add(descriptor))
                    val hit = field.getRetrieverForDescriptors(listOf(descriptor), context).toFlow(this).toList().single()
                    assertEquals(shots[2], hit.id)
                }
            } finally {
                cleanup(schema)
            }
        } finally {
            server.stop(0)
        }
        Unit
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VITRIVR_LIVE_VLM_URL", matches = ".+")
    fun indexesAndRetrievesWithLiveVlm() = runBlocking {
        val schema = loadSchema(
            requireNotNull(System.getenv("VITRIVR_LIVE_VLM_URL")), System.getenv("VITRIVR_LIVE_VLM_MODEL")
        )
        try {
            val shot = seedShot(schema, Color.RED, 100, 200)
            Files.copy(redSample, keyframes.resolve("$shot.png"), StandardCopyOption.REPLACE_EXISTING)
            requireNotNull(schema.getIngestionPipelineBuilder("captions")).build().single().toFlow(this).toList()
            val caption = requireNotNull(schema["vlmcaption"]).getReader().getForRetrievable(shot).singleOrNull()
            checkNotNull(caption) { "The live VLM request did not produce a persisted caption." }
            val query = Json.decodeFromString<InformationNeedDescription>(
                Files.readString(examples.resolve("query-captions.json")).replace("red bicycle", "red")
            )
            val result = QueryResult(QueryParser(schema).parse(query).toFlow(this).toList()).retrievables.singleOrNull()
            checkNotNull(result) { "Query for red did not match the live caption: ${caption.values()["caption"]}" }
            assertEquals(shot.toString(), result.id)
            assertEquals(Value.Long(100), result.descriptors["time.start"])
            assertEquals(Value.Long(200), result.descriptors["time.end"])
            assertTrue(result.score > 0)
        } finally {
            cleanup(schema)
        }
    }

    @Test
    fun sendsTheCompleteShotAndSearchesItsCaption() = runBlocking {
        val requests = AtomicInteger()
        val clip = Files.readAllBytes(videoSample)
        val server = mockVlm(requests, AtomicInteger(-1), expectedVideo = clip)
        try {
            val schema = loadSchema("http://127.0.0.1:${server.address.port}/v1", video = true, maxVideoBytes = clip.size)
            try {
                val shot = seedShot(schema, Color.RED, 10, 20)
                Files.write(keyframes.resolve("$shot.mp4"), clip)
                val oversized = seedShot(schema, Color.GREEN, 30, 40)
                Files.write(keyframes.resolve("$oversized.mp4"), clip + byteArrayOf(0))
                seedShot(schema, Color.BLUE, 50, 60) // A shot with no clip.
                requireNotNull(schema.getIngestionPipelineBuilder("captions")).build().single().toFlow(this).toList()
                assertEquals(1, requests.get())
                val reader = requireNotNull(schema["vlmvideocaption"]).getReader()
                assertEquals(1L, reader.count())
                val stored = reader.getForRetrievable(shot).single()
                assertEquals(Value.Text("The screen changes from red to green."), stored.values()["caption"])
                assertTrue(stored.values()["generation_metadata"]!!.value.toString().contains("video/mp4"))

                server.stop(0)
                verifyVideoQuery(schema, shot, 10, 20)
            } finally {
                cleanup(schema)
            }
        } finally {
            server.stop(0)
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VITRIVR_LIVE_VLM_URL", matches = ".+")
    fun indexesAndRetrievesVideoWithLiveVlm() = runBlocking {
        val schema = loadSchema(
            requireNotNull(System.getenv("VITRIVR_LIVE_VLM_URL")), System.getenv("VITRIVR_LIVE_VLM_MODEL"), video = true
        )
        try {
            val shot = seedShot(schema, Color.RED, 100, 200)
            Files.copy(videoSample, keyframes.resolve("$shot.mp4"))
            requireNotNull(schema.getIngestionPipelineBuilder("captions")).build().single().toFlow(this).toList()
            verifyVideoQuery(schema, shot, 100, 200)
        } finally {
            cleanup(schema)
        }
    }

    private suspend fun CoroutineScope.verifyVideoQuery(schema: Schema, shot: UUID, start: Long, end: Long) {
        val query = Json.decodeFromString<InformationNeedDescription>(Files.readString(examples.resolve("query-video-captions.json")))
        val result = QueryResult(QueryParser(schema).parse(query).toFlow(this).toList()).retrievables.single()
        assertEquals(shot.toString(), result.id)
        assertEquals(Value.Long(start), result.descriptors["time.start"])
        assertEquals(Value.Long(end), result.descriptors["time.end"])
        assertTrue(result.score > 0)
    }

    private fun cleanup(schema: Schema) {
        schema.fields().forEach { it.getInitializer().deinitialize() }
        schema.connection.getRetrievableInitializer().deinitialize()
        schema.close()
    }

    private fun exampleQuery() =
        Json.decodeFromString<InformationNeedDescription>(Files.readString(examples.resolve("query-captions.json")))

    private fun loadSchema(
        baseUrl: String, model: String? = null, video: Boolean = false, maxVideoBytes: Int? = null
    ): Schema {
        val schemaFile = if (video) "video-schema.json" else "schema.json"
        val config = Json.parseToJsonElement(Files.readString(examples.resolve(schemaFile)))
        val example = Json.decodeFromJsonElement<SchemaConfig>(config.jsonObject.getValue("schema").jsonObject.getValue("example"))
        val fieldName = if (video) "vlmvideocaption" else "vlmcaption"
        val resolverName = if (video) "masterClips" else "masterKeyframes"
        val field = example.fields.getValue(fieldName)
        val resolver = example.resolvers.getValue(resolverName)
        val fieldParameters = field.parameters + mapOf("baseUrl" to baseUrl, "model" to (model ?: field.parameters.getValue("model"))) +
            maxVideoBytes?.let { mapOf("maxVideoBytes" to it.toString()) }.orEmpty()
        val schemaConfig = example.copy(
            connection = example.connection.copy(parameters = mapOf(
                "host" to "127.0.0.1",
                "port" to (System.getenv("VITRIVR_TEST_PG_PORT") ?: "5432"),
                "username" to "postgres",
                "password" to "vitrivr"
            )),
            fields = example.fields + mapOf(
                fieldName to field.copy(parameters = fieldParameters),
                "clip" to field.copy(factory = "CLIP", parameters = emptyMap(), indexes = emptyList()),
                "siglip2" to field.copy(factory = "SigLip2", parameters = emptyMap(), indexes = emptyList())
            ),
            resolvers = mapOf(resolverName to resolver.copy(parameters = mapOf("location" to keyframes.toString()))),
            extractionPipelines = emptyMap()
        )
        val manager = SchemaManager()
        val name = "vlmtest_" + UUID.randomUUID().toString().replace("-", "")
        manager.load(name, schemaConfig)
        val schema = requireNotNull(manager.getSchema(name))
        val pipelineFile = if (video) "index-video-captions.json" else "index-captions.json"
        val ingestion = requireNotNull(IngestionConfig.read(examples.resolve(pipelineFile))).copy(schema = name)
        schema.addIngestionPipeline("captions", ingestion)
        schema.connection.getRetrievableInitializer().initialize()
        schema.fields().forEach { it.getInitializer().initialize() }
        return schema
    }

    private fun seedShot(schema: Schema, color: Color, start: Long, end: Long): UUID {
        val id = UUID.randomUUID()
        assertTrue(schema.connection.getRetrievableWriter().add(Ingested(id, "SEGMENT", transient = false)))
        val values = mapOf("start" to Value.Long(start), "end" to Value.Long(end))
        assertTrue(requireNotNull(schema["time"]).getWriter().add(TemporalMetadataDescriptor(UUID.randomUUID(), id, values)))
        val image = BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().let { graphics ->
            graphics.color = color
            graphics.fillRect(0, 0, image.width, image.height)
            graphics.dispose()
        }
        assertTrue(ImageIO.write(image, "png", keyframes.resolve("$id.png").toFile()))
        return id
    }

    private fun mockVlm(
        requests: AtomicInteger, failure: AtomicInteger, expectedVideo: ByteArray? = null
    ): HttpServer {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v1/chat/completions") { exchange ->
            exchange.use {
                val requestId = requests.incrementAndGet()
                val request = Json.parseToJsonElement(it.requestBody.bufferedReader().use { reader -> reader.readText() })
                val media = request.jsonObject.getValue("messages").jsonArray.single().jsonObject.getValue("content")
                    .jsonArray[1].jsonObject
                val type = media.getValue("type").jsonPrimitive.content
                val url = media.getValue(type).jsonObject.getValue("url").jsonPrimitive.content
                val bytes = Base64.getDecoder().decode(url.substringAfter(','))
                val (caption, status) = if (expectedVideo != null) {
                    val matches = type == "video_url" && url.startsWith("data:video/mp4;base64,") &&
                        bytes.contentEquals(expectedVideo)
                    "The screen changes from red to green." to (if (matches) 200 else 400)
                } else {
                    val rgb = ImageIO.read(bytes.inputStream()).getRGB(0, 0)
                    val text = when (rgb) {
                        Color.RED.rgb -> "Red bicycles, red bicycles, red bicycles."
                        Color.GREEN.rgb -> "A red bicycle leans against a wall."
                        else -> "A cat sleeps on a couch."
                    }
                    text to (if (failure.get() == rgb) 503 else 200)
                }
                val body = """{"id":"mock-completion-$requestId","model":"mock-served-model","choices":[{"message":{"content":"$caption"},"finish_reason":"stop"}]}""".toByteArray()
                it.sendResponseHeaders(status, body.size.toLong())
                it.responseBody.write(body)
            }
        }
        server.start()
        return server
    }

    private val examples = Path.of("../example-configs/vlm")
    private val redSample = Path.of("../vitrivr-engine-index/src/test/resources/images/red.png")
    private val videoSample = Path.of("../vitrivr-engine-index/src/test/resources/videos/color_test.mp4")
}
