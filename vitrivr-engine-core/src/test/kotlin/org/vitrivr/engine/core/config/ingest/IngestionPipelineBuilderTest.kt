package org.vitrivr.engine.core.config.ingest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.vitrivr.engine.core.config.ContextFactory
import org.vitrivr.engine.core.config.ingest.operation.OperationConfig
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.blackhole.BlackholeConnectionProvider
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.impl.DiskResolver

class IngestionPipelineBuilderTest {

    @Test
    @DisplayName("Tree Parsing Test Happy Case")
    fun operatorTreeParsingHappyCase() {
        try {
            /* Building the config */
            val config = IngestionConfig(
                "test-schema",
                context = IngestionContextConfig(
                    "InMemoryContentFactory",
                    listOf("disk")
                ),
                operators = mapOf(
                    "enumerator" to OperatorConfig(factory = "FileSystemEnumerator"),
                    "decoder" to OperatorConfig(factory = "ImageDecoder"),
                    "segmenter" to OperatorConfig(factory = "PassThroughSegmenter"),
                    "averagecolor" to OperatorConfig(field = "averagecolor"),
                    "thumbnail" to OperatorConfig(exporter = "thumbnail"),
                    "file" to OperatorConfig(field = "file"),
                    "filter" to OperatorConfig(factory = "TypeFilterTransformer")
                ),
                operations = mapOf(
                    "enumerator" to OperationConfig("enumerator"),
                    "decoder" to OperationConfig("decoder", inputs = listOf("enumerator")),
                    "segmenter" to OperationConfig("segmenter", inputs = listOf("decoder")),
                    "averagecolor" to OperationConfig("averagecolor", inputs = listOf("segmenter")),
                    "thumbnail" to OperationConfig("thumbnail", inputs = listOf("segmenter")),
                    "filter" to OperationConfig("filter", inputs = listOf("averagecolor", "thumbnail")),
                    "file" to OperationConfig("file", inputs = listOf("filter"))
                ),
                output = listOf("file")
            )

            val provider = BlackholeConnectionProvider()
            val mockSchema = Schema("test-schema", BlackholeConnection("test-schema", provider, true))
            mockSchema.addResolver("disk", DiskResolver().newResolver(mockSchema, mapOf("location" to "./thumbnails/testing")))
            val context = ContextFactory.newContext(mockSchema, config.context)
            val testSubject = IngestionPipelineBuilder(config, context)
            testSubject.parseOperations()
        } catch (e: Exception) {
            fail(e)
        }
    }
}
