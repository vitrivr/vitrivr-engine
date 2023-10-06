package org.vitrivr.engine.index.pipeline

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.pipelineConfig.ExtractorConfig
import org.vitrivr.engine.core.config.pipelineConfig.PipelineConfig
import org.vitrivr.engine.core.config.pipelineConfig.SegmenterConfig
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import java.util.*

private val logger: KLogger = KotlinLogging.logger {}


/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class PipelineBuilder(private val schema: Schema, pipelineConfig: PipelineConfig) {


    private var leaves : MutableList<Operator<*>> = mutableListOf()

    init {
        assert(pipelineConfig.schema == schema.name) {
            "Pipeline  name ${pipelineConfig.schema} does not match schema name ${schema.name}"
        }

        val enumeratorConfig = pipelineConfig.enumerator
        val enumerator = (ServiceLoader.load(EnumeratorFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${enumeratorConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Enumerator implementation for '${enumeratorConfig.name}'."))
            .newOperator(enumeratorConfig.parameters, schema)

        logger.info { "Enumerator: ${enumerator.javaClass.name}" }

        val decoderConfig = enumeratorConfig.decoder
        val decoder = (ServiceLoader.load(DecoderFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${decoderConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Decoder implementation for '${decoderConfig.name}'."))
            .newOperator(enumerator, decoderConfig.parameters, schema)

        logger.info { "Decoder: ${decoder.javaClass.name}" }


        val transformerConfig = decoderConfig.transformer
        val transformer = (ServiceLoader.load(TransformerFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${transformerConfig.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Transformer implementation for '${transformerConfig.name}'."))
            .newOperator(decoder, transformerConfig.parameters, schema)

        logger.info { "Transformer: ${transformer.javaClass.name}" }

        addSegmenter(transformer, transformerConfig.segmenters)

    }

    fun addSegmenter(parent: Operator<ContentElement<*>>, segmenterConfigs :List<SegmenterConfig>){
        segmenterConfigs.forEach { segmenterConfig ->
            val segmenter = (ServiceLoader.load(SegmenterFactory::class.java).find {
                it.javaClass.name == "${it.javaClass.packageName}.${segmenterConfig.name}Factory"
            }
                ?: throw IllegalArgumentException("Failed to find Segmenter implementation for '${segmenterConfig.name}'."))
                .newOperator(parent, segmenterConfig.parameters, schema)

            if (segmenterConfig.extractors.isEmpty()) {
                throw IllegalArgumentException("Segmenter '${segmenterConfig.name}' must have at least one extractor.")
            }
            this.addExtractor(segmenter, segmenterConfig.extractors)
            logger.info { "Segmenter: ${segmenter.javaClass.name}" }
        }
    }

    fun addExtractor(parent: Operator<Ingested>, extractorConfigs :List<ExtractorConfig>) {
        extractorConfigs.forEach { extractorConfig ->
            val extractor = (ServiceLoader.load(ExtractorFactory::class.java).find {
                it.javaClass.name == "${it.javaClass.packageName}.${extractorConfig.name}Factory"
            }
                ?: throw IllegalArgumentException("Failed to find Extractor implementation for '${extractorConfig.name}'."))
                .newOperator(parent, extractorConfig.parameters, schema)
            if (extractorConfig.extractors.isNotEmpty()) {
                this.addExtractor(extractor, extractorConfig.extractors)
            } else{
                leaves.add(extractor)
            }
            logger.info { "Extractor: ${extractor.javaClass.name}" }
        }
    }

    fun addExporter(parent: Operator<Ingested>, extractorConfigs :List<ExtractorConfig>) {

    }

    fun getPipeline(): List<Operator<*>> {
        return leaves
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