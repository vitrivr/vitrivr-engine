package org.vitrivr.engine.index.decode

import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.config.ContextFactory
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnectionProvider
import org.vitrivr.engine.core.features.averagecolor.AverageColor
import org.vitrivr.engine.core.features.metadata.source.file.FileSourceMetadata
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.resolver.impl.DiskResolver
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import java.nio.file.Paths
import kotlin.time.Duration

/**
 * A unit test for the [ImageDecoder].
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class ImageDecoderTest {
    @Test
    fun test() = runTest(timeout = Duration.INFINITE) {
        /* Prepare schema. */
        val schema = Schema("test", Paths.get("."),BlackholeConnection("test", BlackholeConnectionProvider()))
        schema.addResolver("test", DiskResolver().newResolver(schema, mapOf()))

        /* Prepare context. */
        val contextConfig = IngestionContextConfig("CachedContentFactory", listOf("test"))
        val context = ContextFactory.newContext(schema, contextConfig).copy(
            local = mapOf(
                "enumerator" to mapOf(
                    "path" to "./src/test/resources/",
                    "mediaTypes" to "IMAGE"
                )
            )
        )

        /* Prepare pipeline. */
        val fileSystemEnumerator = FileSystemEnumerator().newOperator("enumerator", context)
        val decoder = ImageDecoder().newOperator("decoder", input = fileSystemEnumerator, context = context)
        val averageColor =  AverageColor().let { it.newExtractor(schema.Field("averagecolor", it), input = decoder, context = context) }
        val file =  FileSourceMetadata().let { it.newExtractor(schema.Field("file", it), input = averageColor, context = context) }

        /* Execute pipeline. */
        val results = file.toFlow(this).takeWhile { it != TerminalRetrievable }.toList()

        /* Basic sanity checks. */
        Assertions.assertEquals(3, results.size)
        Assertions.assertTrue(results.count { it.type == "SOURCE:IMAGE" } == 3)
        Assertions.assertTrue(results.all { it.relationships.isEmpty() })

        /* The first frame should be red, the second green and the third blue. */
        for (r in results) {
            val fd = r.descriptors.filterIsInstance<FileSourceMetadataDescriptor>().first()
            when (fd.path.value.substringAfterLast("/")) {
                "red.png" -> Assertions.assertArrayEquals(floatArrayOf(1.0f, 0.0f, 0.0f), r.descriptors.filterIsInstance<FloatVectorDescriptor>().first().vector.value, 0.05f)
                "green.png"  -> Assertions.assertArrayEquals(floatArrayOf(0.0f, 1.0f, 0.0f), r.descriptors.filterIsInstance<FloatVectorDescriptor>().first().vector.value, 0.05f)
                "blue.png"  -> Assertions.assertArrayEquals(floatArrayOf(0.0f, 0.0f, 1.0f), r.descriptors.filterIsInstance<FloatVectorDescriptor>().first().vector.value, 0.05f)
            }
        }
    }
}