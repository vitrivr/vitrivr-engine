package org.vitrivr.engine.index.features

import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnectionProvider
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadata
import org.vitrivr.engine.core.model.descriptor.struct.AnyMapStructDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.resolver.impl.DiskResolver
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.index.decode.ImageDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import org.vitrivr.engine.core.features.capturetime.CaptureTime
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * An integration-style test for the [CaptureTime] feature.
 *
 * This test verifies that the CaptureTime extractor correctly parses
 * capture timestamps from image metadata (e.g., EXIF DateTimeOriginal).
 */
class CaptureTimeTest {

    @Test
    fun test() = runTest(timeout = Duration.INFINITE) {

        val schema = Schema("test", BlackholeConnection("test", BlackholeConnectionProvider()))
        schema.addResolver("test", DiskResolver().newResolver(schema, mapOf()))

        val contextConfig = IngestionContextConfig(
            contentFactory = "CachedContentFactory",
            resolvers = listOf("test"),
            global = emptyMap(),
            local = mapOf(
                "enumerator" to mapOf("path" to "./src/test/resources/CaptureTimeImages"),
                "decoder" to mapOf("timeWindowMs" to "1000")
            )
        )
        contextConfig.schema = schema

        val context = IndexContextFactory.newContext(contextConfig)
        val fileSystemEnumerator = FileSystemEnumerator().newEnumerator("enumerator", context, listOf(MediaType.IMAGE))
        val decoder = ImageDecoder().newDecoder("decoder", input = fileSystemEnumerator, context = context)
        val captureTime = CaptureTime().let { it.newExtractor(schema.Field("capturetime", it), input = decoder, context = context) }
        val file = FileSourceMetadata().let { it.newExtractor(schema.Field("file", it), input = captureTime, context = context) }

        val results = file.toFlow(this).takeWhile { it != TerminalRetrievable }.toList()

        assertEquals(2, results.size)
        assertTrue(results.all { it.type == "SOURCE:IMAGE" })
        assertTrue(results.all { it.relationships.isEmpty() })

        var successFound = false
        var noExifFound = false

        for (r in results) {
            val fileDescriptor = r.descriptors.filterIsInstance<FileSourceMetadataDescriptor>().first()
            val captureDescriptor = r.descriptors.filterIsInstance<AnyMapStructDescriptor>().firstOrNull()
            val fileName = fileDescriptor.path.value.substringAfterLast("/")

            println("→ Processed file: $fileName")

            when (fileName) {
                "image_with_capture_time.jpeg" -> {
                    successFound = true
                    assertNotNull(captureDescriptor, "Image with capture time should have a descriptor.")
                    val dateTimeValue = captureDescriptor?.values()?.get("timestamp") as? Value.DateTime
                    assertNotNull(dateTimeValue, "Value.DateTime should not be null.")
                    val expectedDateTime = LocalDateTime.of(2025, 4, 19, 9, 32, 15)
                    assertEquals(expectedDateTime.year, dateTimeValue!!.value.year)
                    assertEquals(expectedDateTime.month, dateTimeValue.value.month)
                    assertEquals(expectedDateTime.dayOfMonth, dateTimeValue.value.dayOfMonth)
                    assertEquals(expectedDateTime.hour, dateTimeValue.value.hour)
                    assertEquals(expectedDateTime.minute, dateTimeValue.value.minute)
                }
                "image_without_exif.jpg" -> {
                    noExifFound = true
                    assertNull(captureDescriptor, "Image with no EXIF data should not have a capture time descriptor.")
                }
                else -> fail("Unexpected file encountered in results: $fileName")
            }
        }

        assertTrue(successFound, "Test image with capture time was not processed.")
        assertTrue(noExifFound, "Test image without EXIF was not processed.")
    }
}