package org.vitrivr.engine.core.config.pipeline

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.IndexConfig
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.config.operators.*
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.util.extension.loadServiceForName

private val logger: KLogger = KotlinLogging.logger {}

/**
 *
 * @author Raphael Waltenspuel
 * @version 1.0
 *
 * Pipleine setup
 * Enumerator: Source -> Decoder: ContentElement<*> -> Transformer: ContentElement<*> -> Segmenter: Ingested -> Extractor: Ingested -> Exporter: Ingested
 */
class PipelineBuilder(schema: Schema, config: IndexConfig) {

    companion object {
        /**
         * Creates a new [PipelineBuilder] using the provided [Schema] and [IndexConfig].
         *
         * @param schema The [Schema] to create a [PipelineBuilder] for.
         * @param config The [IndexConfig] to create a [PipelineBuilder] for.
         */
        fun forConfig(schema: Schema, config: IndexConfig) = PipelineBuilder(schema, config)
    }

    /** List of leaf operators held by this [PipelineBuilder]. */
    private val pipeline: Pipeline = Pipeline()

    init {
        val context = IndexContextFactory.newContext(schema, config.context)
        this.parseEnumerator(config.enumerator, context)
    }

    /**
     * Parses the [Enumerator] stage of the operator pipeline.
     *
     * @param config [EnumeratorConfig] to parse.
     * @param context The [IndexConfig] to use.
     */
    private fun parseEnumerator(config: EnumeratorConfig, context: IndexContext) {
        val factory = loadServiceForName<EnumeratorFactory>(config.name) ?: throw IllegalArgumentException("Failed to find enumerator factory implementation for '${config.name}'.")
        val enumerator: Enumerator = factory.newOperator(context, config.parameters)
        logger.info { "Enumerator: ${enumerator.javaClass.name}" }
        addDecoder(enumerator, context, config.next)
    }

    /**
     * Parses the [Decoder] stage of the operator pipeline.
     *
     * @param parent The [Enumerator] to use as parent.
     * @param context The [IndexConfig] to use.
     * @param config [EnumeratorConfig] to parse.
     */
    private fun addDecoder(parent: Enumerator, context: IndexContext, config: DecoderConfig) {
        val factory = loadServiceForName<DecoderFactory>(config.name) ?: throw IllegalArgumentException("Failed to find decoder factory implementation for '${config.name}'.")
        val decoder = factory.newOperator(parent, context, config.parameters)
        logger.info { "Decoder: ${decoder.javaClass.name}" }
        if (config.nextSegmenter != null) {
            addSegmenter(decoder, context, config.nextSegmenter)
        } else if (config.nextTransformer != null) {
            addTransformer(decoder, context, config.nextTransformer)
        } else {
            throw IllegalArgumentException("Decoder must be followed by either a segmenter or a transformer.")
        }
    }

    /**
     * Parses the [Enumerator] stage of the operator pipeline.
     *
     * @param parent The [Operator] to use as parent.
     * @param context The [IndexConfig] to use.
     * @param config [EnumeratorConfig] to parse.
     */
    private fun addTransformer(parent: Operator<ContentElement<*>>, context: IndexContext, config: TransformerConfig) {
        val factory = loadServiceForName<TransformerFactory>(config.name) ?: throw IllegalArgumentException("Failed to find transformer factory implementation for '${config.name}'.")
        val transformer = when (parent) {
            is Decoder -> factory.newOperator(parent, context, config.parameters)
            is Transformer -> factory.newOperator(parent, context, config.parameters)
            else -> throw IllegalArgumentException("Transformer must be preceded by either a decoder or a transformer.")
        }
        logger.info { "Transformer: ${transformer.javaClass.name}" }
        if (config.nextSegmenter != null) {
            addSegmenter(transformer, context, config.nextSegmenter)
        } else if (config.nextTransformer != null) {
            addTransformer(transformer, context, config.nextTransformer)
        } else {
            throw IllegalArgumentException("Decoder must be followed by either a segmenter or a transformer.")
        }
    }

    /**
     * Parses the [Segmenter] stage of the operator pipeline.
     *
     * @param config [EnumeratorConfig] to parse.
     * @param context The [IndexConfig] to use.
     */
    private fun addSegmenter(parent: Operator<ContentElement<*>>, context: IndexContext, config: SegmenterConfig) {
        val factory = loadServiceForName<SegmenterFactory>(config.name) ?: throw IllegalArgumentException("Failed to find segmenter factory implementation for '${config.name}'.")
        val segmenter = when (parent) {
            is Decoder -> factory.newOperator(parent, context, config.parameters)
            is Transformer -> factory.newOperator(parent, context, config.parameters)
            else -> throw IllegalArgumentException("Segmenter must be preceded by either a decoder or a transformer.")
        }
        logger.info { "Segmenter: ${segmenter.javaClass.name}" }
        for (aggreagtor in config.aggregators) {
            addAggregator(segmenter, context, aggreagtor)
        }
    }

    /**
     * Parses the [Segmenter] stage of the operator pipeline.
     *
     * @param parent The [Segmenter] to use as parent.
     * @param context The [IndexConfig] to use.
     * @param config [EnumeratorConfig] to parse.
     */
    private fun addAggregator(parent: Segmenter, context: IndexContext, config: AggregatorConfig) {
        val factory = loadServiceForName<AggregatorFactory>(config.name) ?: throw IllegalArgumentException("Failed to find aggregator factory implementation for '${config.name}'.")
        val aggregator = factory.newOperator(parent, context, config.parameters)
        logger.info { "Aggregator: ${aggregator.javaClass.name}" }
        if (config.nextExtractor != null) {
            addExtractor(aggregator, context, config.nextExtractor)
        } else if (config.nextExporter != null) {
            addExporter(aggregator, context, config.nextExporter)
        } else {
            throw IllegalArgumentException("Aggregator must be followed by either an extractor or an exporter.")
        }
    }

    /**
     * Parses an [Extractor] stage of the operator pipeline.
     *
     * @param parent The [Operator] to use as parent.
     * @param context The [IndexConfig] to use.
     * @param config [EnumeratorConfig] to parse.
     */
    private fun addExtractor(parent: Operator<Retrievable>, context: IndexContext, config: ExtractorConfig) {
        val extractor = if (config.fieldName != null) {
            val field = context.schema[config.fieldName] ?: throw IllegalArgumentException("Field '${config.parameters["field"]}' does not exist in schema '${context.schema.name}'")
            if (config.parameters.isNotEmpty()) {
                //throw IllegalArgumentException("Extractor '${config.fieldName}' parameters provided in schema '${context.schema.name}' are not supported.")
                logger.warn { "PipelineBuilder overrides Extractor '${config.fieldName}' parameters provided in schema '${context.schema.name}'." }
                field.getExtractor(parent, context, config.parameters)
            } else {
                field.getExtractor(parent, context)
            }
        } else if (config.factoryName != null) {
            val factory = loadServiceForName<Analyser<*, *>>(config.factoryName) ?: throw IllegalArgumentException("Failed to find extractor factory implementation for '${config.factoryName}'.")
            TODO()
        } else {
            throw IllegalArgumentException("Aggregator must be followed by either an extractor or an exporter.")
        }
        logger.info { "Extractor: ${extractor.javaClass.name}" }
        if (config.nextExtractor != null) {
            addExtractor(extractor, context, config.nextExtractor)
        } else if (config.nextExporter != null) {
            addExporter(extractor, context, config.nextExporter)
        } else {
            this.pipeline.addLeaf(extractor)
        }
    }

    /**
     * Parses an [Exporter] stage of the operator pipeline.
     *
     * @param parent The [Operator] to use as parent.
     * @param context The [IndexConfig] to use.
     * @param config [EnumeratorConfig] to parse.
     */
    private fun addExporter(parent: Operator<Retrievable>, context: IndexContext, config: ExporterConfig) {
        val exporter = if (config.exporterName != null) {
            val exporter = context.schema.getExporter(config.exporterName) ?: throw IllegalArgumentException("Exporter '${config.exporterName}' does not exist in schema '${context.schema.name}'")
            if (config.parameters.isNotEmpty()) {
//                throw IllegalArgumentException("Exporter '${config.exporterName}' parameters provided in schema '${context.schema.name}' are not supported.")
                logger.warn { "PipelineBuilder overrides Exporter '${config.exporterName}' parameters provided in schema '${context.schema.name}'." }
                exporter.getExporter(parent, context, config.parameters)
            } else {
                exporter.getExporter(parent, context)
            }
        } else if (config.factoryName != null) {
            val factory = loadServiceForName<ExporterFactory>(config.factoryName) ?: throw IllegalArgumentException("Failed to find extractor factory implementation for '${config.factoryName}'.")
            factory.newOperator(parent, context, config.parameters)
        } else {
            throw IllegalArgumentException("Aggregator must be followed by either an extractor or an exporter.")
        }
        logger.info { "Exporter: ${exporter.javaClass.name}" }
        if (config.nextExtractor != null) {
            addExtractor(exporter, context, config.nextExtractor)
        } else if (config.nextExporter != null) {
            addExporter(exporter, context, config.nextExporter)
        } else {
            this.pipeline.addLeaf(exporter)
        }
    }

    /**
     * Returns the [Operator] pipeline constructed by this [PipelineBuilder].
     *
     * @return [List] of leaf [Operator]s.
     */
    fun getPipeline(): Pipeline = this.pipeline
}