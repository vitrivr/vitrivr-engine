package org.vitrivr.engine.index

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnectionProvider
import org.vitrivr.engine.core.features.averagecolor.AverageColor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.transform.shape.BroadcastOperator
import org.vitrivr.engine.core.operators.transform.shape.CombineOperator
import org.vitrivr.engine.core.resolver.impl.DiskResolver
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.index.aggregators.MiddleContentAggregator
import org.vitrivr.engine.index.decode.VideoDecoder
import org.vitrivr.engine.index.enumerate.FileSystemEnumerator
import kotlin.time.Duration

class PipelineTest {


    @Test
    fun testContentSources() = runTest(timeout = Duration.INFINITE) {
        val schema = Schema("test", BlackholeConnection("test", BlackholeConnectionProvider()))

        schema.addResolver("test", DiskResolver().newResolver(schema, mapOf()))


        val contextConfig = IngestionContextConfig("CachedContentFactory", "test", global = emptyMap(), local = mapOf(
            "enumerator" to mapOf("path" to "./src/test/resources/"),
            "averageColorAgg" to mapOf("contentSources" to "middleAgg")
        ))

        contextConfig.schema = schema

        val context = IndexContextFactory.newContext(contextConfig)

        val fileSystemEnumerator = FileSystemEnumerator().newEnumerator("enumerator", context, listOf(MediaType.VIDEO))

        val decoder = BroadcastOperator(VideoDecoder().newDecoder("decoder", input = fileSystemEnumerator, context = context))

        val middleAgg = MiddleContentAggregator().newTransformer("middleAgg", input = decoder, context = context)

        val averageColor = AverageColor()

        val averageColorAll = averageColor.newExtractor(schema.Field("averageColorAll", averageColor), input = decoder, context = context)

        val averageColorAgg = averageColor.newExtractor(schema.Field("averageColorAgg", averageColor), input = middleAgg, context = context)

        val mergeOp = CombineOperator(listOf(averageColorAll, averageColorAgg))


        val out = mergeOp.toFlow(this).take(6).toList()

        val aggDescriptors = out.flatMap { it.descriptors }.filter { it.field?.fieldName == "averageColorAgg" }

        val allDescriptors = out.flatMap { it.descriptors }.filter { it.field?.fieldName == "averageColorAll" }

        val allContent = out.flatMap { it.content }

        Assertions.assertEquals(allContent.size, allDescriptors.size)

        Assertions.assertEquals(6, aggDescriptors.size)
    }
}