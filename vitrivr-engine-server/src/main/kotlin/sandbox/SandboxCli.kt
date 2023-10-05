package sandbox

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.descriptors.PrimitiveKind
import org.vitrivr.engine.core.config.pipelineConfig.PipelineConfig
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.operators.ingest.templates.*
import org.vitrivr.engine.index.execution.ExecutionServer
import org.vitrivr.engine.index.pipeline.PipelineBuilder
import sandbox.SandboxCli.Companion.runSandboxes
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
                DummyEnumeratorFactory().newOperator(mapOf("enumeratorKey" to "enumeratorValue"), schema)
            val decoder = DummyDecoderFactory().newOperator(enumerator, mapOf("decoderKey" to "decoderValue"), schema)
            val transformer =
                DummyTransformerFactory().newOperator(decoder, mapOf("transformerKey" to "transformerValue"),   schema)
            val segmenter = DummySegmenterFactory().newOperator(transformer, mapOf("segmenterKey" to "segmenterValue"), schema)
            val extractor = DummyExtractorFactory().newOperator(segmenter, mapOf("extractorKey" to "extractorValue"), schema)
            val exporter = DummyExporterFactory().newOperator(extractor, mapOf("exporterKey" to "exporterValue"), schema)

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

        fun flowSandbox(name: String, timeMillis: Long, multiplyer: (Int) -> (Double) = { 1.0 }) = runBlocking<Unit> {
            // Kotlin Flows Introduction
            // https://kotlinlang.org/docs/flow.html

            val flow: Flow<String> = flow { // flow builder
                for (ic in 1..5) {
                    delay(Math.round(timeMillis * multiplyer(ic))) // pretend we are doing something useful here
                    emit("$name $ic") // emit next value
                }
            }
            flow.collect { value -> println(value) }
        }

        fun runSandboxes() = runBlocking<Unit> {
            launch {
                SandboxCli.flowSandbox("Flow Sandbox A", 100)
            }
            launch {
                SandboxCli.flowSandbox("Flow Sandbox B", 200)
            }
            withTimeoutOrNull(250) { // Timeout after 250ms
                SandboxCli.flowSandbox("Flow Sandbox B", 200, { value -> Math.log(value.toDouble()) })
            }
        }
    }
}

fun main(args: Array<String>) {
    runSandboxes()
}