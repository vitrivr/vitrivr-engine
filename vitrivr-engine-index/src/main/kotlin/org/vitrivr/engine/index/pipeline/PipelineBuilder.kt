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

    private var enumerator : Enumerator? = null

    init {
        assert(pipelineConfig.schema == schema.name) {
            "Pipeline  name ${pipelineConfig.schema} does not match schema name ${schema.name}"
        }

        val enumeratorConfig = pipelineConfig.enumerator
        this.enumerator = (ServiceLoader.load(EnumeratorFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${enumeratorConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Enumerator implementation for '${enumeratorConfig.name}'."))
            .createOperator()
        logger.info { "Enumerator: ${this.enumerator!!.javaClass.name}" }

        val decoderConfig = enumeratorConfig.decoder
        val decoder = (ServiceLoader.load(DecoderFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${decoderConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Decoder implementation for '${decoderConfig.name}'."))
            .setSource(this.enumerator!!)
            .createOperator()
        logger.info { "Decoder: ${decoder.javaClass.name}" }


        val transformerConfig = decoderConfig.transformer
        val transformer = (ServiceLoader.load(TransformerFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${transformerConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Transformer implementation for '${transformerConfig.name}'."))
            .setSource(decoder)
            .createOperator()
        logger.info { "Transformer: ${transformer.javaClass.name}" }


        val segmentersConfig = transformerConfig.segmenters
        segmentersConfig.forEach { segmenterConfig ->
            val segmenter = (ServiceLoader.load(SegmenterFactory::class.java).find {
                it.javaClass.name == "${it.javaClass.packageName}.${segmenterConfig.name}Factory"
            }
                ?: throw IllegalArgumentException("Failed to find Segmenter implementation for '${segmenterConfig.name}'."))
                .setSource(transformer)
                .createOperator()
            logger.info { "Segmenter: ${segmenter.javaClass.name}" }

            val extractorsConfig = segmenterConfig.extractors
            extractorsConfig.forEach { extractorConfig ->
                val extractor = (ServiceLoader.load(ExtractorFactory::class.java).find {
                    it.javaClass.name == "${it.javaClass.packageName}.${extractorConfig.name}Factory"
                }
                    ?: throw IllegalArgumentException("Failed to find Extractor implementation for '${extractorConfig.name}'."))
                    .setSource(segmenter)
                    .createOperator()
                logger.info { "Extractor: ${extractor.javaClass.name}" }
            }
        }

    }

    fun getPipeline(): Enumerator {
        return this.enumerator!!
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