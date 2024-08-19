package org.vitrivr.engine.core.config.ingest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.vitrivr.engine.core.config.ingest.operation.OperationConfig
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.context.IngestionContextConfig
import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
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
                    "disk",
                    global = mapOf("location" to "./thumbnails/testing")
                ),
                operators = mapOf(
                    "enumerator" to OperatorConfig.Enumerator("FileSystemEnumerator"),
                    "decoder" to OperatorConfig.Decoder(factory = "ImageDecoder"),
                    "segmenter" to OperatorConfig.Transformer(factory = "PassThroughSegmenter"),
                    "averagecolor" to OperatorConfig.Extractor(fieldName = "averagecolor"),
                    "thumbnail" to OperatorConfig.Exporter(exporterName = "thumbnail"),
                    "file" to OperatorConfig.Extractor(fieldName = "file"),
                    "filter" to OperatorConfig.Transformer(factory = "TypeFilterTransformer")
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

            val mockSchema = Schema("test-schema", BlackholeConnection("test-schema"))
            mockSchema.addResolver("disk", DiskResolver().newResolver(mockSchema, mapOf("location" to "./thumbnails/testing")))
            config.context.schema = mockSchema
            val testSubject = IngestionPipelineBuilder(config)
            testSubject.parseOperations()
        } catch (e: Exception) {
            fail(e)
        }
    }
}
