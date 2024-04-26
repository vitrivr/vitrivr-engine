package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.config.ingest.operation.OperationsConfig
import org.vitrivr.engine.core.config.ingest.operator.*
import org.vitrivr.engine.core.config.pipeline.execution.IndexingPipeline
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.core.util.tree.Tree
import org.vitrivr.engine.core.util.tree.TreeNode
import java.util.*
import java.util.stream.Stream


/**
 * Parses the [IngestionConfig] to build an ingestion pipeline.
 *
 * Currently, there is a strict set of rules regarding the build-up of a pipeline:
 * [Enumerator] -> [Decoder] -> ([Transformer] | [Segmenter])* - if [Segmenter] > [Aggregator]* -> ([Exporter] | [Extractor])*
 */
class IngestionPipelineBuilder(val schema: Schema, val config: IngestionConfig) {

    /**
     * Internal [KLogger] instance for logging
     */
    private val logger: KLogger = KotlinLogging.logger { }

    /**
     * The [IndexingPipeline] this [IngestionPipelineBuilder] builds.
     * Will be populated
     */
    private lateinit var pipeline: IndexingPipeline

    /**
     * Internal definition of the pipeline as a tree.
     */
    val pipelineDefTree = Tree<Pair<String,OperatorConfig>>()

    /**
     * Internal list of [Operator]s
     */
    private val operators = mutableMapOf<String,Operator<*>>()

    /**
     * The [IndexContext]
     */
    private val context = IndexContextFactory.newContext(schema, config.context)

    /**
     * Parses and builds the [Enumerator] based on the given [EnumeratorConfig].
     * Starts the pipeline building process.
     */
    private fun buildEnumerator(name: String, config: EnumeratorConfig, stream: Stream<*>? = null): Enumerator {
        val factory = loadFactory<EnumeratorFactory>(config.factory)
        return if(stream == null){
            factory.newOperator(name, context, config.mediaTypes)
        }else {
            factory.newOperator(name, context, config.mediaTypes, stream)
        }.apply {
            logger.info{"Instantiated new ${if(stream != null){"stream-input"}else{""} } Enumerator: ${this.javaClass.name}"}
        }
    }

    /**
     * Parses and builds the [OperatorConfig] for the [Decoder].
     * Must be invoked after [buildEnumerator]
     */
    private fun buildDecoder(name: String, parent: Enumerator, config: DecoderConfig): Decoder {
//        require(this::enumerator.isInitialized){"Cannot build the decoder before the enumerator. This is a programmer's error!"}
        val factory = loadFactory<DecoderFactory>(config.factory)
        return factory.newOperator(name, parent, context).apply{
            logger.info{"Instantiated new Decoder: ${this.javaClass.name}"}
        }
    }

    /**
     * Parses the pipeline definition, the [IngestionConfig.operations] chain.
     * Builds an internal representation of the definition.
     * Required to be invoked **before** [validatePipelineDefinition]
     */
    fun parseOperations(): List<Tree<Pair<String, OperatorConfig>>> {
        require(pipelineDefTree.isEmpty()) {"Illegal State: Cannot parse the pipeline definition, if there is already a pipeline definition"}
        logger.debug { "Starting building operator tree(s)" }
        /* Find operations without inputs, these are (by definition) enumerators / entry points */
        val entrypoints = config.operations.entries.filter{it.value.isEntry()}
        logger.debug{"Found the following entrypoints: $entrypoints"}
        /* Build trees with entrypoints as roots*/
        return entrypoints.map {
            val tree = Tree<Pair<String,OperatorConfig>>()
            /* Fetch matching operator config */
            val opCfg = config.operators[it.value.operator] ?: throw IllegalArgumentException("No such operator '${it.value.operator}'")
            /* Add root */
            tree.add(TreeNode(it.key, it.value.operator to opCfg))
            /* Iteratively find and add children */
            val stack = Stack<Map.Entry<String, OperationsConfig>>()
            stack.push(it)
            while(stack.isNotEmpty()){
                val ops = stack.pop()
                /* Find children */
                val children = config.operations.entries.filter{x -> x.value.inputs.contains(ops.key)}
                /* Iterate over children. Reversed results in those earlier defined in the list being handled first, due to stack */
                children.reversed().forEach { child ->
                    /* Sanity check if such an operation actually exists */
                    config.operations[child.key] ?: throw IllegalArgumentException("No such operations '$child'")
                    stack.push(child)
                    /* Add child as node / leaf to tree */
                    val cOpCfg = config.operators[child.value.operator] ?: throw IllegalArgumentException("No such operator '${it.value.operator}'")
                    tree.addTo(ops.key, TreeNode(child.key, child.value.operator to cOpCfg))
                }
            }
            return@map tree
        }.apply{
            logger.debug{"Found and build ${this.size} operation tree(s). Root(s) is / are enumerator(s)"}
        }

    }

    /**
     * Validates the pipeline definition.
     * This method should throw exceptions for any violation of the rules for a pipeline and guarantee that the pipeline
     * is fit for construction upon completion.
     *
     * Currently, for pipeline (and 'stage'):
     * ```
     * Enumerator -> Decoder -> (Transformer | Segmenter)* -Segmenter ?> Aggregator* -> (Extractor | Exporter)*
     * 0             1           2                                       4              5
     * ```
     */
    private fun validatePipelineDefinition(pipelineDefTree: Tree<Pair<String, OperatorConfig>>) {
        require(!pipelineDefTree.isEmpty()) { "Illegal State: Pipeline definition does not (yet) exist. This is a programmer's error!" }
        logger.debug { "Validating pipeline definition." }
        var phase = 0
        var counter = 0
        pipelineDefTree.depthFirstPreorder { node, parent ->
            val operatorConfig = node.value.second
            logger.debug{"Validating  ${node.name} (${counter++}-th) operation entry: $operatorConfig. We are in phase: $phase"}
            /* 1. Phase determination, if we are deeper than root */
            if(parent != null){
                val last = parent.value.second
                if(last.type == OperatorType.ENUMERATOR && operatorConfig.type == OperatorType.DECODER){
                    phase = 1
                }else if (last.type == OperatorType.DECODER && (operatorConfig.type == OperatorType.TRANSFORMER || operatorConfig.type == OperatorType.SEGMENTER) ){
                    phase = 2
                }else if (last.type == OperatorType.SEGMENTER && operatorConfig.type == OperatorType.AGGREGATOR) {
                    phase = 4
                } else if (last.type == OperatorType.AGGREGATOR && (operatorConfig.type == OperatorType.EXPORTER || operatorConfig.type == OperatorType.EXTRACTOR)) {
                    phase = 5
                }
                logger.debug{"Determined phase according to environment: $phase"}
            }else{
                phase = 0
            }
            /* 2. Phase boundary conditions */
            when (phase) {
                0 -> if (operatorConfig.type != OperatorType.ENUMERATOR) {
                    throw IllegalArgumentException("Pipeline is in stage ENUMERATOR but found ${operatorConfig.type}")
                }
                1 -> if (operatorConfig.type != OperatorType.DECODER) {
                    throw IllegalArgumentException("Pipeline is in stage DECODER but found ${operatorConfig.type}")
                }
                2 -> {
                    if (operatorConfig.type != OperatorType.TRANSFORMER && operatorConfig.type != OperatorType.SEGMENTER) {
                        throw IllegalArgumentException("Pipeline is in stage TRANSFORMER / SEGMENTER but found ${operatorConfig.type}")
                    }
                    if(operatorConfig.type != OperatorType.SEGMENTER && node.children.isNotEmpty() ){
                        throw IllegalArgumentException("Pipeline branching only allowed for SEGMENTER, but branches with ${operatorConfig.type}")
                    }
                }

                4 -> if (operatorConfig.type != OperatorType.AGGREGATOR) {
                    throw IllegalArgumentException("Pipeline is in stage AGGREGATOR but found ${operatorConfig.type}")
                }

                5 -> if (operatorConfig.type != OperatorType.EXTRACTOR && operatorConfig.type != OperatorType.EXPORTER) {
                    throw IllegalArgumentException("Pipeline is in stage EXTRACTOR / EXPORTER but found ${operatorConfig.type}")
                }
                else -> throw IllegalStateException("Pipeline is in an unknown stage $phase. This is a programmer's error!")
            }
        }
        logger.debug{"Validation complete."}
    }

    /**
     * Parses the pipeline and constructs the corresponding operators.
     */
    private fun buildOperators(pipelineDefTree: Tree<Pair<String, OperatorConfig>>, stream: Stream<*>? = null){
        require(this::pipeline.isInitialized) {"Illegal State: Cannot build the ingestion pipeline when the pipeline is not initialised. This is a programmer's error!"}
        pipelineDefTree.depthFirstPreorder { node, parent ->
            logger.debug{"Building operator ${node.value.first} for operation ${node.name}"}
            val op = if(parent == null){
                /* Current node is the root, e.g. an enumerator */
                buildOperator(node.value.first, null, node.value.second, stream)
            }else{
                val parentOp = operators[parent.value.first] ?: throw IllegalArgumentException("Could not find operator with name ${parent.name}")
                buildOperator(node.value.first, parentOp, node.value.second, stream)
            }
            operators[node.value.first] = op
            if(node.isLeaf()){
                @Suppress("UNCHECKED_CAST")
                pipeline.addLeaf(op as Operator<Retrievable>)
                logger.debug{"Added the the operator ${node.value.first} to the pipeline"}
            }else{
                logger.debug{"Built the operator named ${node.value.first}"}
            }
        }
    }

    /**
     * Build the [Operator] based on the provided [OperatorConfig] at the specified position.
     *
     * @param name The name of the [Operator] to build.
     * @param parent The parent operator. Requires the very first one to be a [Decoder]
     * @param config The [OperatorConfig] which holds information on how to build the [Operator]
     */
    private fun buildOperator(name: String, parent: Operator<*>?, config: OperatorConfig, stream: Stream<*>? = null): Operator<*>{
        require(this::pipeline.isInitialized) {"Illegal State: Cannot build the ingestion pipeline if the pipeline is not initialised. This is a programmer's error!"}
        logger.debug { "Building operator for configuration $config" }
        @Suppress("UNCHECKED_CAST")
        return when(config.type){ // the when-on-type is on purpose: It enforces all branches
            OperatorType.ENUMERATOR -> buildEnumerator(name, config as EnumeratorConfig, stream)
            OperatorType.DECODER -> buildDecoder(name, parent as Enumerator, config as DecoderConfig)
            OperatorType.OPERATOR -> throw UnsupportedOperationException("Free-form OPERATOR operators are not yet supported")
            OperatorType.RETRIEVER -> throw UnsupportedOperationException("RETRIEVER operators are not yet supported")
            OperatorType.TRANSFORMER -> buildTransformer(name, parent!!, config as TransformerConfig)
            OperatorType.EXTRACTOR -> buildExtractor(parent as Operator<Retrievable>, config as ExtractorConfig) // Unchecked cast SHOULD(tm) be fine due to validation of pipeline
            OperatorType.EXPORTER -> buildExporter(name, parent as Operator<Retrievable>, config as ExporterConfig) // Unchecked cast SHOULD(tm) be fine due to validation of pipeline
            OperatorType.AGGREGATOR -> buildAggregator(name, parent!!, config as AggregatorConfig)
            OperatorType.SEGMENTER -> buildSegmenter(name, parent!!, config as SegmenterConfig)
        }
    }

    /**
     * Builds a [Transformer] based on the [TransformerConfig]'s factory.
     *
     * @param name The name of the [Transformer]
     * @param parent: The preceding [Operator]. Has to be one of: [Decoder], [Transformer]
     * @param config: The [TransformerConfig] describing the to-be-built [Transformer]
     */
    private fun buildTransformer(name: String, parent: Operator<*>, config: TransformerConfig): Transformer {
        require(parent is Decoder || parent is Transformer){"Preceding a transformer, there must be decoder or transformer, but found $parent"}
        val factory = loadFactory<TransformerFactory>(config.factory)
        return when(parent){
            is Decoder -> factory.newOperator(name, parent, context)
            is Transformer -> factory.newOperator(name, parent, context)
            else -> throw IllegalArgumentException("Cannot build transformer succeeding $parent")
        }.apply {
            logger.info{"Built transformer: ${this.javaClass.name} with name $name"}
        }
    }

    /**
     * Builds a [Segmenter] based on the [SegmenterConfig]'s factory.
     *
     * @param name The name of the [Segmenter]
     * @param parent: The preceding [Operator]. Has to be one of: [Decoder], [Transformer]
     * @param config: The [SegmenterConfig] describing the to-be-built [Segmenter]
     */
    private fun buildSegmenter(name: String, parent: Operator<*>, config: SegmenterConfig): Segmenter{
        require(parent is Decoder || parent is Transformer){"Preceding a segmenter, there must be a decoder or transformer, but found $parent"}
        val factory = loadFactory<SegmenterFactory>(config.factory)
        return when(parent){
            is Decoder -> factory.newOperator(name, parent, context,)
            is Transformer -> factory.newOperator(name, parent, context)
            else -> throw IllegalArgumentException("Cannot build segmenter succeeding $parent")
        }.apply{
            logger.info{"Built segmenter: ${this.javaClass.name} with name $name"}
        }
    }

    /**
     * Builds an [Aggregator] based on the [AggregatorConfig]'s factory.
     *
     * @param name The name of the [Aggregator]
     * @param parent: The preceding [Operator]. Has to be one of: [Aggregator]
     * @param config: The [AggregatorConfig] describing the to-be-built [Aggregator]
     */
    private fun buildAggregator(name: String, parent: Operator<*>, config: AggregatorConfig): Aggregator{
        require(parent is Segmenter){"Preceding an aggregator, there must be a segmenter"}
        val factory = loadFactory<AggregatorFactory>(config.factory)
        return factory.newOperator(name, parent, context)
            .apply{
                logger.info{"Built aggregator: ${this.javaClass.name} with name $name"}
            }
    }

    /**
     * Builds an [Exporter] based on the [ExporterConfig]'s factory OR the [ExporterConfig.exporterName] named exporter in the schema.
     *
     * @param name The name of the [Exporter]
     * @param parent: The preceding [Operator]. Has to be one of: [Exporter], [Extractor], [Aggregator].
     * @param config: The [ExporterConfig] describing the to-be-built [Exporter]
     */
    private fun buildExporter(name: String, parent: Operator<Retrievable>, config: ExporterConfig): Exporter {
        require(parent is Exporter || parent is Extractor<*, *> || parent is Aggregator) { "Preceding an exporter, there must be an aggregator, exporter or extractor, but found $parent" }
        return if (config.exporterName.isNullOrBlank() && !config.factory.isNullOrBlank()) {
            /* Case factory is specified */
            val factory = loadFactory<ExporterFactory>(config.factory)
            factory.newOperator(name, parent, context).apply {
                logger.info { "Built exporter from factory: ${this.javaClass.name} with name $name" }
            }
        } else {
            /* Case exporter name is given. Due to require in ExporterConfig.init, this is fine as an if-else */
            val exporter =
                context.schema.getExporter(config.exporterName!!) ?: throw IllegalArgumentException(
                    "Exporter '${config.exporterName}' does not exist on schema '${context.schema.name}'"
                )
            exporter.getExporter(parent, context).apply {
                logger.info { "Built exporter by name from schema: ${this.javaClass.name} with name $name" }
            }
        }
    }

    /**
     * Builds an [Extractor] based on the [ExtractorConfig.fieldName] named field in the schema.
     *
     * @param parent The preceding [Operator]. Has to be one of: [Exporter], [Extractor].
     * @param config: The [ExtractorConfig] describing the to-be-built [Extractor]
     */
    private fun buildExtractor(
        parent: Operator<Retrievable>,
        config: ExtractorConfig
    ): Extractor<*, *> {
        require(parent is Exporter || parent is Extractor<*, *> || parent is Aggregator) { "Preceding an extractor, there must be an exporter or extractor, but found $parent" }
        val field = context.schema[config.fieldName]
            ?: throw IllegalArgumentException("Field '${config.fieldName}' does not exist in schema '${context.schema.name}'")
        return field.getExtractor(parent, context).apply {
                logger.info { "Built extractor by name from schema: ${this.javaClass.name}" }
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

    /**
     * Build the [IndexingPipeline] based this [IngestionPipelineBuilder]'s [config].
     *
     * The involved steps are:
     * 1. Parsing of the configuration
     * 2. Validating the configuration
     * 3. Building the operators
     *
     * @param stream If there is a specific stream the built [Enumerator] should process.
     * @return The produced [IndexingPipeline], ready to be processed.
     */
    fun build(stream: Stream<*>? = null): IndexingPipeline{
        this.pipeline = IndexingPipeline()
        parseOperations().forEach {
            /* Validation first, would throw an exception*/
            validatePipelineDefinition(it)
            /* buildOperators fills the pipeline automagically */
            buildOperators(it, stream)
        }
        return pipeline
    }
    
}
