package sandbox

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.pipelineConfig.PipelineConfig
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.operators.ingest.templates.*
import org.vitrivr.engine.index.execution.ExecutionServer
import org.vitrivr.engine.index.pipeline.PipelineBuilder
import java.nio.file.Paths
import kotlin.system.exitProcess

private val logger: KLogger = KotlinLogging.logger {}

class SandboxCli {

    companion object {
        fun extarctionSandbox(schema: Schema) {
            logger.info { "Starting extraction sandbox for schema '${schema.name}'." }
            val executionServer = ExecutionServer();

            val pipeline = PipelineConfig.read(Paths.get(PipelineConfig.DEFAULT_PIPELINE_PATH)) ?: exitProcess(1)
            assert(
                schema.name.equals(
                    pipeline.schema,
                    ignoreCase = true
                )
            ) { "Pipeline schema '${pipeline.schema}' does not match schema '${schema.name}'." }
            logger.trace { "Successfully initialized schema '${schema.name}' and pipeline '${pipeline.schema}'." }

            val enumerator =
                DummyEnumeratorFactory().newOperator(mapOf("enumeratorKey" to "enumeratorValue"))
            val decoder = DummyDecoderFactory().newOperator(enumerator, mapOf("decoderKey" to "decoderValue"))
            val transformer =
                DummyTransformerFactory().newOperator(decoder, mapOf("transformerKey" to "transformerValue"))
            val segmenter = DummySegmenterFactory().newOperator(transformer, mapOf("segmenterKey" to "segmenterValue"))
            val extractor = DummyExtractorFactory().newOperator(segmenter, mapOf("extractorKey" to "extractorValue"))
            val exporter = DummyExporterFactory().newOperator(extractor, mapOf("exporterKey" to "exporterValue"))

            // Logging Test
            logger.trace { "Trace is set" }
            logger.debug { "Debug is set" }
            logger.info { "Info is set" }
            logger.warn { "Warn is set" }
            logger.error { "Error is set" }

            // Operators test TODO: Remove
//            executionServer.addOperator(enumerator)
//            executionServer.addOperator(decoder)
//            executionServer.addOperator(transformer)
//            executionServer.addOperator(segmenter)
//            executionServer.addOperator(extractor)

            val pipelineBuilder = PipelineBuilder.forConfig(schema, pipeline)

            executionServer.addOperatorPipeline(pipelineBuilder)
            executionServer.execute()
            executionServer.shutdown()
            logger.info { "Finished extraction sandbox for schema '${schema.name}'." }
        }
    }
}
