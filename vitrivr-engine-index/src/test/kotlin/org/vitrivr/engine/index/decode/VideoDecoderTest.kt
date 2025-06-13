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
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.resolver.impl.DiskResolver
import org.vitrivr.engine.index.aggregators.content.MiddleContentAggregator
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import java.nio.file.Paths
import kotlin.time.Duration

/**
 * A unit test for the [VideoDecoder].
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
class VideoDecoderTest {
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
                    "path" to "./src/test/resources/videos",
                    "mediaTypes" to "VIDEO"
                ),
                "decoder" to mapOf(
                    "timeWindowMs" to "1000"
                )
            )
        )

        /* Prepare pipeline. */
        val fileSystemEnumerator = FileSystemEnumerator().newOperator("enumerator", context)
        val decoder = VideoDecoder().newOperator("decoder", input = fileSystemEnumerator, context = context)
        val aggregator = MiddleContentAggregator().newOperator("middle", input = decoder, context = context)
        val averageColor =  AverageColor().let { it.newExtractor(schema.Field("averagecolor", it), input = aggregator, context = context) }

        /* Execute pipeline. */
        val results = averageColor.toFlow(this).takeWhile { it != TerminalRetrievable }.toList()

        /* Basic sanity checks. */
        Assertions.assertEquals(4, results.size)
        Assertions.assertTrue(results.count { it.type == "SOURCE:VIDEO" } == 1)
        Assertions.assertTrue(results.count { it.type == "SEGMENT" } == 3)

        /* The first frame should be red, the second green and the third blue. */
        for ((i, r) in results.withIndex()) {
            when (i) {
                0 -> Assertions.assertArrayEquals(floatArrayOf(1.0f, 0.0f, 0.0f), r.descriptors.filterIsInstance<FloatVectorDescriptor>().first().vector.value, 0.05f)
                1 -> Assertions.assertArrayEquals(floatArrayOf(0.0f, 1.0f, 0.0f), r.descriptors.filterIsInstance<FloatVectorDescriptor>().first().vector.value, 0.05f)
                2 -> Assertions.assertArrayEquals(floatArrayOf(0.0f, 0.0f, 1.0f), r.descriptors.filterIsInstance<FloatVectorDescriptor>().first().vector.value, 0.05f)
            }
        }
    }
}