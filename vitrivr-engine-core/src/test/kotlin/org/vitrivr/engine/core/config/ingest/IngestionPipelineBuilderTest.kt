package org.vitrivr.engine.core.config.ingest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.vitrivr.engine.core.config.ContextConfig
import org.vitrivr.engine.core.config.ingest.operation.OperationConfig
import org.vitrivr.engine.core.config.ingest.operator.*
import org.vitrivr.engine.core.model.metamodel.MockSchema

class IngestionPipelineBuilderTest {

    @Test
    @DisplayName("Tree Parsing Test Happy Case")
    fun operatorTreeParsingHappyCase() {
        try {
            /* Building the config */
            val config = IngestionConfig(
                "test-schema",
                context = ContextConfig(
                    "InMemoryContentFactory",
                    "DiskResolver",
                    mapOf("location" to "./thumbnails/testing")
                ),
                //mapOf("C2" to mapOf("maxSideResolution" to "350"))
                enumerator = EnumeratorConfig(
                    "FileSystemEnumerator",
                    mapOf(
                        "path" to "./testing/source",
                        "mediaTypes" to "IMAGE;VIDEO",
                        "depth" to "1"
                    )
                ),
                decoder = DecoderConfig(factory = "ImageDecoder"),
                operators = mapOf(
                    "A" to SegmenterConfig(factory = "PassThroughSegmenter"),
                    "B1" to AggregatorConfig(factory = "AllContentAggregator"),
                    "B2" to AggregatorConfig(factory = "AllContentAggregator"),
                    "C1" to ExtractorConfig(fieldName = "averagecolor"),
                    "C2" to ExporterConfig(
                        exporterName = "thumbnail",
                        parameters = mapOf("maxSideResolution" to "350", "mimeType" to "JPG")
                    ),
                    "D1" to ExtractorConfig(fieldName = "file")
                ),
                operations = mapOf(
                    "stage1" to OperationConfig(
                        "A",
                        listOf("stage2-1", "stage2-2")
                    ),
                    "stage2-1" to OperationConfig("B1", listOf("stage3-1")),
                    "stage2-2" to OperationConfig("B2", listOf("stage3-2")),
                    "stage3-1" to OperationConfig("C1", listOf("stage4")),
                    "stage3-2" to OperationConfig("C2"),
                    "stage4" to OperationConfig("D1")
                )
            )

            val mockSchema = MockSchema()
            val testSubject = IngestionPipelineBuilder(mockSchema, config)
            testSubject.parseOperations()
        } catch (e: Exception) {
            fail(e)
        }

    }
}
