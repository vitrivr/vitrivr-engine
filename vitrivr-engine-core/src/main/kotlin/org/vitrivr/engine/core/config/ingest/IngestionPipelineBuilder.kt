package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.config.ingest.operator.*
import org.vitrivr.engine.core.config.pipeline.execution.IndexingPipeline
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.util.extension.loadServiceForName


/**
 * Parses the [IngestionConfig] to build an ingestion pipeline.
 */
class IngestionPipelineBuilder(val schema: Schema, val config: IngestionConfig) {

    /**
     * Internal [KLogger] instance for logging
     */
    private val logger: KLogger = KotlinLogging.logger { }

    /**
     * The constructed [Enumerator]
     */
    private lateinit var enumerator: Enumerator

    /**
     * The constructed [Decoder]
     */
    private lateinit var decoder: Decoder

    /**
     * The [IndexingPipeline] this [IngestionPipelineBuilder] builds.
     * Will be populated
     */
    private lateinit var pipeline: IndexingPipeline

    /**
     * Internal definition of the pipeline in form of ordered list of [OperatorConfig]s
     */
    private val pipelineDef = mutableListOf<OperatorConfig>()

    /**
     * Internal flag whether [pipelineDef] is valid.
     */
    private var pipelineDefValid = false

    /**
     * Internal list of [Operator]s
     */
    private val operators = mutableListOf<Operator<*>>()

    /**
     * The [IndexContext]
     */
    private val context = IndexContextFactory.newContext(schema, config.context)

    /**
     * Parses and builds the [Enumerator] based on the given [EnumeratorConfig].
     * Starts the pipeline building process.
     */
    private fun buildEnumerator(config: EnumeratorConfig, context: IndexContext){
        val factory = loadFactory<EnumeratorFactory>(config.factory)
        enumerator = factory.newOperator(context, config.parameters)
        logger.info{"Instantiated new Enumerator: ${enumerator.javaClass.name}, parameters: ${config.parameters}"}
    }

    /**
     * Parses and builds the [OperatorConfig] for the [Decoder].
     * Must be invoked after [buildEnumerator]
     */
    private fun buildDecoder(config: DecoderConfig, context: IndexContext){
        require(this::enumerator.isInitialized){"Cannot build the decoder before the enumerator. This is a programmer's error!"}
        val factory = loadFactory<DecoderFactory>(config.factory)
        decoder = factory.newOperator(enumerator, context, config.parameters)
        logger.info{"Instantiated new Decoder: ${decoder.javaClass.name}, parameters: ${config.parameters}"}
    }

    /**
     * Parses the pipeline definition, the [IngestionConfig.operations] chain.
     * Builds an internal representation of the definition.
     * Required to be invoked **before** [validatePipelineDefinition]
     */
    private fun parseOperations(){
        require(pipelineDef.isEmpty()) {"Illegal State: Cannot parse the pipeline definition, if there is already a pipeline definition"}
        logger.debug { "Starting building operator tree" }
        config.operations.entries.forEachIndexed { index, entry ->
            logger.debug { "Parsing $index-th operation: ${entry.key}" }
            val opCfg = config.operators[entry.value.operator] ?: throw IllegalArgumentException("No such operator '${entry.value.operator}")
            logger.debug{ "Found operator config for operation ${entry.key}: $opCfg"}
            pipelineDef.add(opCfg)
        }
    }

    /**
     * Validates the pipeline definition.
     * This method should throw exceptions for any violation of the rules for a pipeline and guarantee that the pipeline
     * is fit for construction upon completion.
     *
     * Currently, for pipeline (and 'stage'):
     * ```
     * (Transformer | Segmenter)* -Segmenter ?> Aggregator* -> (Extractor | Exporter)*
     * 0                                        1               2
     * ```
     */
    private fun validatePipelineDefinition() {
        require(pipelineDef.isNotEmpty()) { "Cannot " }
        logger.debug { "Validating pipeline definition." }
        var phase = 0
        pipelineDef.forEachIndexed { index, operatorConfig ->
            logger.debug{"Validating $index-th operation entry $operatorConfig. We are in phase: $phase"}
            /* 1. Phase determination */
            if (index >= 1) {
                val last = pipelineDef[index - 1]
                if (last.type == OperatorType.SEGMENTER && operatorConfig.type == OperatorType.AGGREGATOR) {
                    phase = 1
                } else if (last.type == OperatorType.AGGREGATOR && (operatorConfig.type == OperatorType.EXPORTER || operatorConfig.type == OperatorType.EXTRACTOR)) {
                    phase = 2
                }
            }
            logger.debug{"Determined phase according to environment: $phase"}
            /* 2. Phase boundary conditions */
            when (phase) {
                0 -> if (operatorConfig.type != OperatorType.TRANSFORMER && operatorConfig.type != OperatorType.SEGMENTER) {
                    throw IllegalArgumentException("Pipeline is in stage TRANSFORMER / SEGMENTER but found ${operatorConfig.type}")
                }

                1 -> if (operatorConfig.type != OperatorType.AGGREGATOR) {
                    throw IllegalArgumentException("Pipeline is in stage AGGREGATOR but found ${operatorConfig.type}")
                }

                2 -> if (operatorConfig.type != OperatorType.EXTRACTOR && operatorConfig.type != OperatorType.EXPORTER) {
                    throw IllegalArgumentException("Pipeline is in stage EXTRACTOR / EXPORTER but found ${operatorConfig.type}")
                }
            }
            if(operatorConfig.type == OperatorType.ENUMERATOR || operatorConfig.type == OperatorType.DECODER){
                throw IllegalArgumentException("ENUMERATOR and DECODER are specified outside of the pipeline.")
            }
        }
        logger.debug{"Validation complete."}
        pipelineDefValid=true
    }

    /**
     * Parses the pipeline and constructs the corresponding operators.
     */
    private fun buildOperators(){
        require(this::pipeline.isInitialized) {"Illegal State: Cannot build the ingestion pipeline if the pipeline is not initialised. This is a programmer's error!"}
        require(pipelineDefValid) {"Illegal State: Cannot build the ingestion pipeline if not previously validated. This is a programmer's error!"}
        var previous : Operator<*>
        pipelineDef.forEachIndexed { idx, entry ->
            logger.debug { "Building the $idx-th operator" }
            if(idx == 0){
                /* First operation, preceded by Enumerator -> Decoder */
            }else if(idx == config.operations.size-1){
                /* Last operation, finalise */
            }else{
                /* Operation in between, build */
            }
        }
    }

    /**
     * Build the [Operator] based on the provided [OperatorConfig] at the specified position.
     *
     */
    private fun buildOperator(index: Int, config: OperatorConfig): Operator<*>{
        require(this::pipeline.isInitialized) {"Illegal State: Cannot build the ingestion pipeline if the pipeline is not initialised. This is a programmer's error!"}
        require(index in 0..pipelineDef.size){"Illegal Argument: Index has to be within pipeline bounds. This is a programmer's error!"}
        logger.debug { "Building $index-th operator for configuration $config" }
        return when(config.type){ // the when-on-type is on purpose: It enforces all branches
            OperatorType.ENUMERATOR -> throw IllegalStateException("Paring operators and encountered an ENUMERATOR, which should start the pipeline. This is a configuration error! Use the configuration's 'enumerator' property!")
            OperatorType.DECODER -> throw IllegalStateException("Paring operators and encountered an DECODER, which should start the pipeline. This is a configuration error! Use the configuration's 'decoder' property!")
            OperatorType.OPERATOR -> throw UnsupportedOperationException("Free-form OPERATOR operators are not yet supported")
            OperatorType.RETRIEVER -> throw UnsupportedOperationException("RETRIEVER operators are not yet supported")
            OperatorType.TRANSFORMER -> buildTransformer(operators[index-1], config as TransformerConfig)
            OperatorType.EXTRACTOR -> TODO()
            OperatorType.EXPORTER -> TODO()
            OperatorType.AGGREGATOR -> buildAggregator(operators[index-1], config as AggregatorConfig)
            OperatorType.SEGMENTER -> buildSegmenter(operators[index-1], config as SegmenterConfig)
        }
    }

    /**
     * Builds a [Transformer] based on the [TransformerConfig]'s factory.
     * @param parent: The preceding [Operator]
     * @param config: The [TransformerConfig] describing the to-be-built [Transformer]
     */
    private fun buildTransformer(parent: Operator<*>, config: TransformerConfig): Transformer {
        require(parent is Decoder || parent is Transformer){"Preceding a transformer, there must be decoder or transformer"}
        val factory = loadFactory<TransformerFactory>(config.factory)
        return when(parent){
            is Decoder -> factory.newOperator(parent, context, config.parameters)
            is Transformer -> factory.newOperator(parent, context, config.parameters)
            else -> throw IllegalArgumentException("Cannot build transformer succeeding $parent")
        }.apply {
            logger.info{"Built transformer: ${this.javaClass.name} with parameters ${config.parameters}"}
        }
    }

    /**
     * Builds a [Segmenter] based on the [SegmenterConfig]'s factory.
     * @param parent: The preceding [Operator]
     * @param config: The [SegmenterConfig] describing the to-be-built [Segmenter]
     */
    private fun buildSegmenter(parent: Operator<*>, config: SegmenterConfig): Segmenter{
        require(parent is Decoder || parent is Transformer){"Preceding a segmenter, there must be a decoder or transformer"}
        val factory = loadFactory<SegmenterFactory>(config.factory)
        return when(parent){
            is Decoder -> factory.newOperator(parent, context, config.parameters)
            is Transformer -> factory.newOperator(parent, context, config.parameters)
            else -> throw IllegalArgumentException("Cannot build segmenter succeeding $parent")
        }.apply{
            logger.info{"Built segmenter: ${this.javaClass.name} with parameters ${config.parameters}"}
        }
    }

    /**
     * Builds a [Aggregator] based on the [AggregatorConfig]'s factory.
     * @param parent: The preceding [Operator]
     * @param config: The [AggregatorConfig] describing the to-be-built [Aggregator]
     */
    private fun buildAggregator(parent: Operator<*>, config: AggregatorConfig): Aggregator{
        require(parent is Segmenter){"Preceding an aggregator, there must be a segmenter"}
        val factory = loadFactory<AggregatorFactory>(config.factory)
        return factory.newOperator(parent, context, config.parameters)
            .apply{
                logger.info{"Built aggregator: ${this.javaClass.name} with parameters ${config.parameters}"}
            }
    }

    private fun buildExporter(parent: Operator<Retrievable>, config: ExporterConfig): Exporter {
        require(parent is Exporter || parent is Extractor<*, *> || parent is Aggregator) { "Preceding an exporter, there must be an aggregator, exporter or extractor" }
        return if (config.exporterName.isNullOrBlank() && !config.factory.isNullOrBlank()) {
            /* Case factory is specified */
            val factory = loadFactory<ExporterFactory>(config.factory!!)
            factory.newOperator(parent, context, config.parameters).apply {
                logger.info { "Built exporter from factory: ${this.javaClass.name} with parameters ${config.parameters}" }
            }
        } else {
            /* Case exporter name is given. Due to require in ExporterConfig.init, this is fine as an if-else */
            val exporter =
                context.schema.getExporter(config.exporterName!!) ?: throw IllegalArgumentException(
                    "Exporter '${config.exporterName}' does not exist on schema '${context.schema.name}'"
                )
            if (config.parameters.isNotEmpty()) {
                logger.warn { "PipelineBuilder overrides exporter '${config.exporterName}' parameters for extraction." }
                exporter.getExporter(parent, context, config.parameters)
            } else {
                exporter.getExporter(parent, context)
            }.apply {
                logger.info { "Built exporter by name from schema: ${this.javaClass.name} with parameters ${config.parameters}" }
            }
        }
    }

    /**
     * Loads the appropriate factory for the given name.
     *
     * @param name The (fully qualified or simple) class name of a factory.
     * @throws IllegalArgumentException If the class could not be found: Either the simple name is not unique or there exists no such class.
     */
    private inline fun <reified T: Any> loadFactory(name: String): T {
        return loadServiceForName<T>(name) ?: throw IllegalArgumentException("Failed to find '${T::class.java.simpleName}' implementation for name '$name'")
    }

    fun build(): IndexingPipeline{
        this.pipeline = IndexingPipeline()
        buildEnumerator(config.enumerator, context)
        buildDecoder(config.decoder, context)
        parseOperations()
        validatePipelineDefinition()
        buildOperators()
        return pipeline
    }
    
}
