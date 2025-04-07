package org.vitrivr.engine.index

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
import org.vitrivr.engine.core.operators.transform.shape.BroadcastOperator
import org.vitrivr.engine.core.operators.transform.shape.CombineOperator
import org.vitrivr.engine.core.resolver.impl.DiskResolver
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.index.aggregators.content.MiddleContentAggregator
import org.vitrivr.engine.index.decode.VideoDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import kotlin.time.Duration

/**
 * Test cases for the extaction pipeline.
 */
class PipelineTest {

    /**
     * A small test case that tests the functionality of the pipeline and the ability of operators to filter content by content source.
     *
     * Idea of the test is as follows:
     * - We construct a manual pipeline that extracts a 3s example video showing a red, green and blue screen for 1s each.
     * - We extract the [AverageColor] value for the content (once for each frame, and once aggregated).
     * - We check that the extracted values are correct.
     */
    @Test
    fun testContentSources() = runTest(timeout = Duration.INFINITE) {
        val schema = Schema("test", BlackholeConnection("test", BlackholeConnectionProvider()))

        schema.addResolver("test", DiskResolver().newResolver(schema, mapOf()))


        val contextConfig = IngestionContextConfig(
            "CachedContentFactory", listOf("test"), global = emptyMap(),
            local = mapOf(
                "enumerator" to mapOf("path" to "./src/test/resources/"),
                "decoder" to mapOf("timeWindowMs" to "1000"),
                "averageColorAgg" to mapOf("contentSources" to "middleAgg"),
            )
        )

        contextConfig.schema = schema

        val context = IndexContextFactory.newContext(contextConfig)

        val fileSystemEnumerator = FileSystemEnumerator().newEnumerator("enumerator", context, listOf(MediaType.VIDEO))

        val decoder = BroadcastOperator(VideoDecoder().newDecoder("decoder", input = fileSystemEnumerator, context = context))

        val middleAgg = MiddleContentAggregator().newTransformer("middleAgg", input = decoder, context = context)

        val averageColor = AverageColor()

        val averageColorAll = averageColor.newExtractor(schema.Field("averageColorAll", averageColor), input = decoder, context = context)

        val averageColorAgg = averageColor.newExtractor(schema.Field("averageColorAgg", averageColor), input = middleAgg, context = context)

        val mergeOp = CombineOperator(listOf(averageColorAll, averageColorAgg))

        /* We expect three results (6s video, 1000ms window). */
        val out = mergeOp.toFlow(this).takeWhile { it != TerminalRetrievable }.toList()

        /* Now extract all content elements and descriptors for each branch. */
        val aggDescriptors = out.flatMap { it.descriptors }.filterIsInstance<FloatVectorDescriptor>().filter { it.field?.fieldName == "averageColorAgg" }
        val allDescriptors = out.flatMap { it.descriptors }.filterIsInstance<FloatVectorDescriptor>().filter { it.field?.fieldName == "averageColorAll" }

        /* The number of total descriptors should be equal to the number of content elements (72 frames for 3s). */
        Assertions.assertEquals(72, allDescriptors.size)

        /* The first 24 frames should be R, followed by G and B. */
        for (i in allDescriptors.indices) {
            if (i < 24) {
                Assertions.assertArrayEquals(floatArrayOf(1.0f, 0.0f, 0.0f), allDescriptors[i].vector.value, 0.05f)
            } else if (i < 48) {
                Assertions.assertArrayEquals(floatArrayOf(0.0f, 1.0f, 0.0f), allDescriptors[i].vector.value, 0.05f)
            } else {
                Assertions.assertArrayEquals(floatArrayOf(0.0f, 0.0f, 1.0f), allDescriptors[i].vector.value, 0.05f)
            }
        }

        /* The number of aggregated descriptors should be equal to the number of content elements. */
        Assertions.assertEquals(3, aggDescriptors.size)

        /* The descriptors should find a full R value followed by G and B. */
        Assertions.assertArrayEquals(floatArrayOf(1.0f, 0.0f, 0.0f), aggDescriptors[0].vector.value, 0.05f)
        Assertions.assertArrayEquals(floatArrayOf(0.0f, 1.0f, 0.0f), aggDescriptors[1].vector.value, 0.05f)
        Assertions.assertArrayEquals(floatArrayOf(0.0f, 0.0f, 1.0f), aggDescriptors[2].vector.value, 0.05f)
    }
}