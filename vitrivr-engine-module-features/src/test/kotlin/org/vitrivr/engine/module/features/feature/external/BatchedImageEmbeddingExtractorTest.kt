package org.vitrivr.engine.module.features.feature.external

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.module.features.feature.external.implementations.clip.CLIP
import org.vitrivr.engine.module.features.feature.external.implementations.clip.CLIPExtractor
import org.vitrivr.engine.module.features.feature.external.implementations.siglip2.SigLip2
import org.vitrivr.engine.module.features.feature.external.implementations.siglip2.SigLip2Extractor
import java.awt.image.BufferedImage
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO

class BatchedImageEmbeddingExtractorTest {
    @Test
    fun keepsInputOrderAndFlushesTheRemainderBeforeTheTerminal() = runBlocking {
        val shots = listOf(shot(), shot(10, 20), shot(30), shot(40))
        for (model in listOf("clip", "siglip2")) {
            val calls = CopyOnWriteArrayList<Pair<String, List<Int>>>()
            withServer(calls) { host ->
                val input = input(shots + TerminalRetrievable)
                val extractor = if (model == "clip") {
                    CLIPExtractor(input, CLIP(), null, "clip", host, 3)
                } else {
                    SigLip2Extractor(input, SigLip2(), null, "siglip2", host, 3)
                }
                val output = extractor.toFlow(this).toList()
                assertEquals(listOf("batch" to listOf(10, 20, 30), "batch" to listOf(40)), calls.toList())
                assertEquals(shots.map { it.id }, output.dropLast(1).map { it.id })
                assertEquals(
                    listOf(emptyList(), listOf(10, 20), listOf(30), listOf(40)),
                    output.dropLast(1).map(::values)
                )
                assertSame(TerminalRetrievable, output.last())
            }
        }
    }

    @Test
    fun fallsBackToSingleImagesWhenTheBatchEndpointIsUnavailable() = runBlocking {
        val calls = CopyOnWriteArrayList<Pair<String, List<Int>>>()
        val shots = listOf(shot(51), shot(72))
        withServer(calls, acceptBatch = false) { host ->
            val extractor = SigLip2Extractor(input(shots), SigLip2(), null, "siglip2", host, 2)
            assertEquals(listOf(listOf(51), listOf(72)), extractor.toFlow(this).toList().map(::values))
            assertEquals(listOf("batch" to listOf(51, 72), "single" to listOf(51), "single" to listOf(72)), calls.toList())
        }
    }

    private fun shot(vararg red: Int) = Ingested(UUID.randomUUID(), "SEGMENT", content = red.map { value ->
        InMemoryImageContent(BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).apply { setRGB(0, 0, value shl 16) })
    }, transient = false)

    private fun input(shots: List<Retrievable>) = object : Operator.Nullary<Retrievable> {
        override val name = "test"
        override fun toFlow(scope: CoroutineScope) = flowOf(*shots.toTypedArray())
    }

    private fun values(shot: Retrievable) = shot.descriptors.filterIsInstance<FloatVectorDescriptor>().map { descriptor ->
        assertEquals(shot.id, descriptor.retrievableId)
        descriptor.vector.value.single().toInt()
    }

    private suspend fun withServer(
        calls: MutableList<Pair<String, List<Int>>>, acceptBatch: Boolean = true, run: suspend (String) -> Unit
    ) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/extract/") { exchange ->
            exchange.use {
                val data = it.requestBody.bufferedReader().use { reader -> reader.readText() }
                val batched = it.requestURI.path.endsWith("_batch")
                val urls = if (batched) {
                    Json.parseToJsonElement(data).jsonObject.getValue("images").jsonArray.map { item -> item.jsonPrimitive.content }
                } else {
                    listOf(URLDecoder.decode(data.substringAfter("data="), UTF_8))
                }
                val markers = urls.map { url ->
                    ImageIO.read(Base64.getDecoder().decode(url.substringAfter(',')).inputStream()).getRGB(0, 0) shr 16 and 0xff
                }
                calls.add((if (batched) "batch" else "single") to markers)
                val response = (if (batched) Json.encodeToString(markers.map(::listOf)) else Json.encodeToString(markers)).toByteArray()
                it.sendResponseHeaders(if (batched && !acceptBatch) 404 else 200, response.size.toLong())
                it.responseBody.write(response)
            }
        }
        server.start()
        try {
            run("http://127.0.0.1:${server.address.port}/")
        } finally {
            server.stop(0)
        }
    }
}
