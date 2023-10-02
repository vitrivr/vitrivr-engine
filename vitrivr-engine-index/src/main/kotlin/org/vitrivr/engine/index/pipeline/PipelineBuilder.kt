package org.vitrivr.engine.index.pipeline

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.pipelineConfig.PipelineConfig
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.ingest.*
import java.util.*
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger {}


/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class PipelineBuilder(private val schema: Schema, pipelineConfig: PipelineConfig) {

    private var enumerator : Enumerator

    init {
        assert(pipelineConfig.schema == schema.name) {
            "Pipeline  name ${pipelineConfig.schema} does not match schema name ${schema.name}"
        }

        val enumeratorConfig = pipelineConfig.enumerator
        this.enumerator = (ServiceLoader.load(EnumeratorFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${enumeratorConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Enumerator implementation for '${enumeratorConfig.name}'."))
            .newOperator(enumeratorConfig.parameters)
        logger.info { "Enumerator: ${this.enumerator.javaClass.name}" }

        val decoderConfig = enumeratorConfig.decoder
        val decoder = (ServiceLoader.load(DecoderFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${decoderConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Decoder implementation for '${decoderConfig.name}'."))
            .newOperator(this.enumerator, decoderConfig.parameters)
        logger.info { "Decoder: ${decoder.javaClass.name}" }


        val transformerConfig = decoderConfig.transformer
        val transformer = (ServiceLoader.load(TransformerFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${transformerConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Transformer implementation for '${transformerConfig.name}'."))
            .newOperator(decoder, transformerConfig.parameters)
        logger.info { "Transformer: ${transformer.javaClass.name}" }


        val segmentersConfig = transformerConfig.segmenters
        segmentersConfig.forEach { segmenterConfig ->
            val segmenter = (ServiceLoader.load(SegmenterFactory::class.java).find {
                it.javaClass.name == "${it.javaClass.packageName}.${segmenterConfig.name}Factory"
            }
                ?: throw IllegalArgumentException("Failed to find Segmenter implementation for '${segmenterConfig.name}'."))
                .newOperator(transformer, segmenterConfig.parameters)
            logger.info { "Segmenter: ${segmenter.javaClass.name}" }

            val extractorsConfig = segmenterConfig.extractors
            extractorsConfig.forEach { extractorConfig ->
                val extractor = (ServiceLoader.load(ExtractorFactory::class.java).find {
                    it.javaClass.name == "${it.javaClass.packageName}.${extractorConfig.name}Factory"
                }
                    ?: throw IllegalArgumentException("Failed to find Extractor implementation for '${extractorConfig.name}'."))
                    .newOperator(segmenter, extractorConfig.parameters)
                logger.info { "Extractor: ${extractor.javaClass.name}" }
            }
        }

    }

    fun getPipeline(): Enumerator {
        return this.enumerator
    }

    companion object {
        /**
         * Creates a new [PipelineBuilder] using the provided [Schema] and [PipelineConfig].
         *
         * @param schema The [Schema] to create a [PipelineBuilder] for
         * @param
         */
        fun forConfig(schema: Schema, pipelineConfig: PipelineConfig) = PipelineBuilder(schema, pipelineConfig)
    }

}