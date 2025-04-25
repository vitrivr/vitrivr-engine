package org.vitrivr.engine.index.decode

import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnectionProvider
import org.vitrivr.engine.core.features.averagecolor.AverageColor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.resolver.impl.DiskResolver
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.index.aggregators.content.MiddleContentAggregator
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import kotlin.time.Duration

/**
 * A unit test for the [VideoDecoder].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class VideoDecoderTest {
    @Test
    fun test() = runTest(timeout = Duration.INFINITE) {
        /* Prepare schema. */
        val schema = Schema("test", BlackholeConnection("test", BlackholeConnectionProvider()))
        schema.addResolver("test", DiskResolver().newResolver(schema, mapOf()))

        /* Prepare context. */
        val contextConfig = IngestionContextConfig(
            "CachedContentFactory", listOf("test"), global = emptyMap(),
            local = mapOf(
                "enumerator" to mapOf("path" to "./src/test/resources/videos"),
                "decoder" to mapOf("timeWindowMs" to "1000")
            )
        )
        contextConfig.schema = schema

        /* Prepare pipeline. */
        val context = IndexContextFactory.newContext(contextConfig)
        val fileSystemEnumerator = FileSystemEnumerator().newEnumerator("enumerator", context, listOf(MediaType.VIDEO))
        val decoder = VideoDecoder().newDecoder("decoder", input = fileSystemEnumerator, context = context)
        val aggregator = MiddleContentAggregator().newTransformer("middle", input = decoder, context = context)
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