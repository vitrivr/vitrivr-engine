package org.vitrivr.engine.module.features.feature.external

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.model.retrievable.attributes.SourceAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.time.ShotAudioRangeAttribute
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.file.FileSource
import org.vitrivr.engine.core.source.file.MimeType
import org.vitrivr.engine.module.features.feature.external.implementations.asr.AutomaticSpeechRecognition
import org.vitrivr.engine.module.features.feature.external.implementations.asr.AutomaticSpeechRecognitionExtractor
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class ContinuousVideoAsrTest {
    @Test
    fun transcribesEachVideoOnceAndAssignsBoundaryWordsToTheFollowingShot(@TempDir directory: Path) = runBlocking {
        val source = FileSource(path = Files.writeString(directory.resolve("test.mp4"), "test"), mimeType = MimeType.MP4)
        val requests = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/extract/asr/video") { exchange ->
            exchange.use {
                requests.incrementAndGet()
                val uploaded = it.requestBody.readAllBytes().decodeToString()
                val body = """
                    [{"midpointNs":49,"text":" one"},{"midpointNs":50,"text":" two"},
                     {"midpointNs":50,"text":" three"},{"midpointNs":200,"text":" outside"}]
                """.trimIndent().toByteArray()
                it.sendResponseHeaders(if (uploaded == "test") 200 else 400, body.size.toLong())
                it.responseBody.write(body)
            }
        }
        server.start()
        try {
            val shots = listOf(0L to 50L, 50L to 100L, 100L to 150L).map { (start, end) ->
                Ingested(
                    UUID.randomUUID(), "SEGMENT", emptyList(),
                    attributes = setOf(SourceAttribute(source), ShotAudioRangeAttribute(start, end)),
                    transient = false
                )
            }
            val input = object : Operator.Nullary<Retrievable> {
                override val name = "test"
                override fun toFlow(scope: CoroutineScope) = (shots + TerminalRetrievable).asFlow()
            }
            val extractor = AutomaticSpeechRecognitionExtractor(
                input, AutomaticSpeechRecognition(), "asr", "http://127.0.0.1:${server.address.port}",
                continuousVideo = true
            )
            val results = extractor.toFlow(this).toList()
            assertEquals(1, requests.get())
            assertEquals((shots + TerminalRetrievable).map { it.id }, results.map { it.id })
            assertEquals(
                listOf("one", "two three", null, null),
                results.map { it.descriptors.filterIsInstance<TextDescriptor>().singleOrNull()?.value?.value }
            )
        } finally {
            server.stop(0)
        }
        Unit
    }
}
