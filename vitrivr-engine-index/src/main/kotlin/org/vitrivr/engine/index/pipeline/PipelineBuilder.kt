package org.vitrivr.engine.index.pipeline

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.ExporterConfig
import org.vitrivr.engine.core.config.pipelineConfig.*
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.source.Source
import java.util.*

private val logger: KLogger = KotlinLogging.logger {}


/**
 *
 * @author Raphael Waltenspuel
 * @version 1.0
 *
 * Pipleine setup
 * Enumerator: Source -> Decoder: ContentElement<*> -> Transformer: ContentElement<*> -> Segmenter: Ingested -> Extractor: Ingested -> Exporter: Ingested
 */
class PipelineBuilder(private val schema: Schema, pipelineConfig: PipelineConfig) {


    private var leaves: MutableList<Operator<*>> = mutableListOf()

    init {
        assert(pipelineConfig.schema == schema.name) {
            "Pipeline  name ${pipelineConfig.schema} does not match schema name ${schema.name}"
        }

        addEnumerator(pipelineConfig.enumerator)


    }

    fun addEnumerator(config: EnumeratorConfig) {
        val enumerator = (ServiceLoader.load(EnumeratorFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${config.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Enumerator implementation for '${config.name}'."))
            .newOperator(config.parameters, schema)

        logger.info { "Enumerator: ${enumerator.javaClass.name}" }
        addDecoder(enumerator, config.decoder)
    }

    fun addDecoder(parent: Operator<Source>, config: DecoderConfig) {

        val decoder = (ServiceLoader.load(DecoderFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${config.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Decoder implementation for '${config.name}'."))
            .newOperator(parent, config.parameters, schema)

        logger.info { "Decoder: ${decoder.javaClass.name}" }
        addTransformer(decoder, config.transformer)
    }

    fun addTransformer(parent: Operator<ContentElement<*>>, config: TransformerConfig) {
        val transformer = (ServiceLoader.load(TransformerFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${config.name}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Transformer implementation for '${config.name}'."))
            .newOperator(parent, config.parameters, schema)

        logger.info { "Transformer: ${transformer.javaClass.name}" }
        addSegmenter(transformer, config.segmenters)
    }

    fun addSegmenter(parent: Operator<ContentElement<*>>, configs: List<SegmenterConfig>) {
        configs.forEach { segmenterConfig ->
            val segmenter = (ServiceLoader.load(SegmenterFactory::class.java).find {
                it.javaClass.name == "${it.javaClass.packageName}.${segmenterConfig.name}Factory"
            }
                ?: throw IllegalArgumentException("Failed to find Segmenter implementation for '${segmenterConfig.name}'."))
                .newOperator(parent, segmenterConfig.parameters, schema)

            if (segmenterConfig.childs.isEmpty()) {
                throw IllegalArgumentException("Segmenter '${segmenterConfig.name}' must have at least one extractor.")
            }
            this.addIngestedPipeline(segmenter, segmenterConfig.childs)
            logger.info { "Segmenter: ${segmenter.javaClass.name}" }
        }
    }

    fun addIngestedPipeline(parent: Operator<Ingested>, configs: List<Any>) {

        when (configs.firstOrNull()) {
            is ExtractorConfig -> addExtractor(parent, configs as List<ExtractorConfig>)
            is ExporterConfig -> addExporter(parent, configs as List<ExporterConfig>)
            else -> throw IllegalArgumentException("IngestedPipelineConfig must be either a list of ExporterConfig or a list of IngestedPipelineConfig")
        }
    }

    fun addExtractor(parent: Operator<Ingested>, configs: List<ExtractorConfig>) {

        configs.forEach { config ->
            val extractor = (schema.get(config.parameters["field"] as String)
                ?: throw IllegalArgumentException("Field '${config.parameters["field"]}' does not exist in schema '${schema.name}'")
                    //  TODO Add non schema extractor
                    //   val extractor = (ServiceLoader.load(ExtractorFactory::class.java).find {
                    //                it.javaClass.name == "${it.javaClass.packageName}.${config.name}Factory"
                    //            }

                    ).getExtractor(parent)

            if (config.childs.isNotEmpty()) {
                this.addIngestedPipeline(extractor, config.childs)
            } else {
                leaves.add(extractor)
            }
            logger.info { "Extractor: ${extractor.javaClass.name}" }
        }
    }

    fun addExporter(parent: Operator<Ingested>, configs: List<ExporterConfig>) {
        configs.forEach { config ->
            val exporter = (schema.getExporter(config.parameters["exporters"] as String)
                ?: throw IllegalArgumentException("Field '${config.parameters["field"]}' does not exist in schema '${schema.name}'")
                    //  TODO Add non schema extractor
                    //   val extractor = (ServiceLoader.load(ExtractorFactory::class.java).find {
                    //                it.javaClass.name == "${it.javaClass.packageName}.${config.name}Factory"
                    //            }

                    ).getExporter(parent)

            if (config.childs.isNotEmpty()) {
                this.addIngestedPipeline(exporter, config.childs)
            } else {
                leaves.add(exporter)
            }
            logger.info { "Exporter: ${exporter.javaClass.name}" }
        }
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