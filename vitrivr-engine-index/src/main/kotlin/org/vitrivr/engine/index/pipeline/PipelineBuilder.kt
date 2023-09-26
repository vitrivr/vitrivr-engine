package org.vitrivr.engine.index.pipeline

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.pipelineConfig.PipelineConfig
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import java.util.*
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger {}


/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class PipelineBuilder(private val schema: Schema, pipelineConfig: PipelineConfig) {

    private val operators: MutableList<Operator<*>> = mutableListOf()

    init {
        assert(pipelineConfig.schema == schema.name) {
            "Pipeline  name ${pipelineConfig.schema} does not match schema name ${schema.name}"
        }

        val enumeratorConfig = pipelineConfig.enumerator
        val enumerator = (ServiceLoader.load(OperatorFactory.EnumeratorFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${enumeratorConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Enumerator implementation for '${enumeratorConfig.name}'."))
            .createOperator()
        logger.info { "Enumerator: ${enumerator.javaClass.name}" }

        val decoderConfig = enumeratorConfig.decoder
        val decoder = (ServiceLoader.load(OperatorFactory.DecoderFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${decoderConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Decoder implementation for '${decoderConfig.name}'."))
            .setSource(enumerator)
            .createOperator()
        logger.info { "Decoder: ${decoder.javaClass.name}" }


        val transformerConfig = decoderConfig.transformer
        val transformer = (ServiceLoader.load(OperatorFactory.TransformerFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${transformerConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Transformer implementation for '${transformerConfig.name}'."))
            .setSource(decoder)
            .createOperator()
        logger.info { "Transformer: ${transformer.javaClass.name}" }


        val segmentersConfig = transformerConfig.segmenters
        segmentersConfig.forEach { segmenterConfig ->
            val segmenter = (ServiceLoader.load(OperatorFactory.SegmenterFactory::class.java).find {
                it.javaClass.name == "${it.javaClass.packageName}.${segmenterConfig.name}Factory"
            }
                ?: throw IllegalArgumentException("Failed to find Segmenter implementation for '${segmenterConfig.name}'."))
                .setSource(transformer)
                .createOperator()
            logger.info { "Segmenter: ${segmenter.javaClass.name}" }

            val extractorsConfig = segmenterConfig.extractors
            extractorsConfig.forEach { extractorConfig ->
                val extractor = (ServiceLoader.load(OperatorFactory.ExtractorFactory::class.java).find {
                    it.javaClass.name == "${it.javaClass.packageName}.${extractorConfig.name}Factory"
                }
                    ?: throw IllegalArgumentException("Failed to find Extractor implementation for '${extractorConfig.name}'."))
                    .setSource(segmenter)
                    .createOperator()
                logger.info { "Extractor: ${extractor.javaClass.name}" }
            }
        }
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

    /** The [Enumerator] instance this [PipelineBuilder] is pointing to. */
    private var child: Enumerator? = null

    /**
     * Appends an [Enumerator] to the [PipelineBuilder].
     *
     * @param enumerator The [KClass] specifying the [org.vitrivr.engine.core.operators.ingest.Enumerator].
     * @param parameters A [Map] of parameters.
     * @return [PipelineBuilder.Enumerator]
     */
    fun withEnumerator(
        enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Enumerator>,
        parameters: Map<String, Any>
    ): PipelineBuilder.Enumerator {
        this.child = Enumerator(enumerator, parameters)
        return this.child!!
    }

    /**
     * An [Enumerator] stage in the pipeline described by the surrounding [PipelineBuilder].
     */
    inner class Enumerator internal constructor(
        private val enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Enumerator>,
        private val parameters: Map<String, Any>
    ) {

        /** The (child) [Decoder] instance this [PipelineBuilder.Enumerator] is pointing to. */
        private var child: Decoder? = null

        /**
         * Appends an [Decoder] to the [Enumerator].
         *
         * @param decoder The [KClass] specifying the [org.vitrivr.engine.core.operators.ingest.Enumerator].
         * @param parameters A [Map] of parameters.
         * @return [PipelineBuilder.Enumerator]
         */
        fun withDecoder(
            decoder: KClass<org.vitrivr.engine.core.operators.ingest.Decoder>,
            parameters: Map<String, Any>
        ): PipelineBuilder.Decoder {
            this.child = Decoder(decoder, parameters)
            return this.child!!
        }

    }

    /**
     * An [Decoder] stage in the pipeline described by the surrounding [PipelineBuilder].
     */
    inner class Decoder internal constructor(
        private val enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Decoder>,
        private val parameters: Map<String, Any>
    ) {

        /** The (child) [Decoder] instance this [PipelineBuilder.Enumerator] is pointing to. */
        private var child: Transformer? = null

        /**
         * Appends an [Decoder] to the [Enumerator].
         *
         * @param decoder The [KClass] specifying the [org.vitrivr.engine.core.operators.ingest.Enumerator].
         * @param parameters A [Map] of parameters.
         * @return [PipelineBuilder.Enumerator]
         */
        fun withTransformer(
            decoder: KClass<org.vitrivr.engine.core.operators.ingest.Decoder>,
            parameters: Map<String, Any>
        ) = Decoder(decoder, parameters)
    }

    /**
     *
     */
    inner class Transformer internal constructor(
        private val enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Transformer>,
        private val parameters: Map<String, Any>
    ) {

    }
}