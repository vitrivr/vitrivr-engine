package org.vitrivr.engine.index.features

import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
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
import org.vitrivr.engine.module.features.feature.lsc.timestamp.LSCTimestamp
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * An integration-style test for the [LSCTimestamp] feature.
 *
 * This test runs a small pipeline to verify that the LSCTimestampExtractor correctly
 * parses the 'minute_id' from JPEG comment metadata.
 */
class LSCTimestampTest {

    @Test
    fun test() = runTest(timeout = Duration.INFINITE) {

        val schema = Schema("test", BlackholeConnection("test", BlackholeConnectionProvider()))
        schema.addResolver("test", DiskResolver().newResolver(schema, mapOf()))


        val contextConfig = IngestionContextConfig(
            contentFactory = "CachedContentFactory",
            resolvers = listOf("test"),
            global = emptyMap(),
            local = mapOf(
                "enumerator" to mapOf("path" to "./src/test/resources/TimestampImages"),
                "decoder" to mapOf("timeWindowMs" to "1000")
            )
        )
        contextConfig.schema = schema

        val context = IndexContextFactory.newContext(contextConfig)
        val fileSystemEnumerator = FileSystemEnumerator().newEnumerator("enumerator", context, listOf(MediaType.IMAGE))
        val decoder = ImageDecoder().newDecoder("decoder", input = fileSystemEnumerator, context = context)
        val lsctimestamp = LSCTimestamp().let { it.newExtractor(schema.Field("lsctimestamp", it), input = decoder, context = context) }
        val file =  FileSourceMetadata().let { it.newExtractor(schema.Field("file", it), input = lsctimestamp, context = context) }

        val results = file.toFlow(this).takeWhile { it != TerminalRetrievable }.toList()


        Assertions.assertEquals(2, results.size)
        Assertions.assertTrue(results.count { it.type == "SOURCE:IMAGE" } == 2)
        Assertions.assertTrue(results.all {it.relationships.isEmpty()})


        var successFound = false
        var noCommentFound = false

        for (r in results) {
            val fileDescriptor = r.descriptors.filterIsInstance<FileSourceMetadataDescriptor>().first()
            val timestampDescriptor = r.descriptors.filterIsInstance<AnyMapStructDescriptor>().firstOrNull()
            val fileName = fileDescriptor.path.value.substringAfterLast("/")

            println("→ Processed file: $fileName")

            when (fileName) {
                "image_with_lsc_timestamp.jpg" -> {
                    successFound = true
                    assertNotNull(timestampDescriptor, "Image with LSC timestamp should have a descriptor.")
                    val dateTimeValue = timestampDescriptor?.values()?.get("minuteIdTimestamp") as? Value.DateTime
                    assertNotNull(dateTimeValue, "Value.DateTime should not be null.")
                    // Assert if the parsed LocalDateTime is correct
                    val expectedDateTime = LocalDateTime.of(2019, 7, 21, 15, 9)
                    assertEquals(expectedDateTime, dateTimeValue!!.value, "Parsed LocalDateTime from minute_id is incorrect.")
                }
                "image_without_comment.jpeg" -> {
                    noCommentFound = true
                    assertNull(timestampDescriptor, "Image with no comment should not have a timestamp descriptor.")
                }
                else -> {
                    fail("Unexpected file encountered in results: $fileName")
                }
            }
        }

        assertTrue(successFound, "Test image with LSC timestamp was not processed.")
        assertTrue(noCommentFound, "Test image without comment was not processed.")
    }
}