package org.vitrivr.engine.module.features.feature.external

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryImageContent
import org.vitrivr.engine.core.model.descriptor.scalar.TextDescriptor
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.module.features.feature.external.implementations.ocr.OpticalCharacterRecognition
import org.vitrivr.engine.module.features.feature.external.implementations.ocr.OpticalCharacterRecognitionExtractor
import java.awt.image.BufferedImage
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentOcrExtractorTest {
    @Test
    fun boundsConcurrentRequestsAndPreservesOrderBeforeTheTerminal() = runBlocking {
        val pool = Executors.newFixedThreadPool(4)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val concurrent = AtomicInteger()
        val highest = AtomicInteger()
        val overlap = CountDownLatch(2)
        server.executor = pool
        server.createContext("/extract/ocr") { exchange ->
            val current = concurrent.incrementAndGet()
            highest.accumulateAndGet(current, ::maxOf)
            overlap.countDown()
            try {
                exchange.use {
                    it.requestBody.readAllBytes()
                    val body = """{"text":"recognized"}""".toByteArray()
                    it.sendResponseHeaders(if (overlap.await(3, TimeUnit.SECONDS)) 200 else 400, body.size.toLong())
                    it.responseBody.write(body)
                }
            } finally {
                concurrent.decrementAndGet()
            }
        }
        server.start()
        try {
            val image = InMemoryImageContent(BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB))
            val shots = List(5) { Ingested(UUID.randomUUID(), "SEGMENT", listOf(image), transient = false) }
            val input = object : Operator.Nullary<Retrievable> {
                override val name = "test"
                override fun toFlow(scope: CoroutineScope) = flowOf(*(shots + TerminalRetrievable).toTypedArray())
            }
            val extractor = OpticalCharacterRecognitionExtractor(
                input, OpticalCharacterRecognition(), null, "ocr", "http://127.0.0.1:${server.address.port}", 2
            )
            val results = extractor.toFlow(this).toList()
            assertEquals(2, highest.get())
            assertEquals((shots + TerminalRetrievable).map { it.id }, results.map { it.id })
            assertTrue(results.dropLast(1).all { shot ->
                val text = shot.descriptors.filterIsInstance<TextDescriptor>().single()
                text.retrievableId == shot.id && text.value.value == "recognized"
            })
        } finally {
            server.stop(0)
            pool.shutdownNow()
        }
        Unit
    }
}
