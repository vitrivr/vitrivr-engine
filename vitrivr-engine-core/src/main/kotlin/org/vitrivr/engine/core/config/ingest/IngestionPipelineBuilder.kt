package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.IndexContextFactory
import org.vitrivr.engine.core.config.ingest.operation.Operation
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Exporter
import org.vitrivr.engine.core.operators.general.ExporterFactory
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.operators.sinks.DefaultSink
import org.vitrivr.engine.core.operators.transform.shape.BroadcastOperator
import org.vitrivr.engine.core.operators.transform.shape.CombineOperator
import org.vitrivr.engine.core.operators.transform.shape.ConcatOperator
import org.vitrivr.engine.core.operators.transform.shape.MergeOperator
import org.vitrivr.engine.core.operators.transform.shape.MergeType.*
import org.vitrivr.engine.core.util.extension.loadServiceForName
import java.util.stream.Stream

/**  Internal [KLogger] instance for logging */
private val logger: KLogger = KotlinLogging.logger { }

/**
 * Parses the [IngestionConfig] in the context of a given [Schema] to construct an ingestion pipeline.
 *
 *  @author Loris Sauter
 *  @author Ralph Gasser
 *  @version 2.1.0
 */
class IngestionPipelineBuilder(val config: IngestionConfig) {

    /** The [IndexContext] */
    private val context = IndexContextFactory.newContext(config.context)

    /**
     * Build the indexing based this [IngestionPipelineBuilder]'s [config].
     *
     * The involved steps are:
     * 1. Parsing of the configuration
     * 2. Validating the configuration
     * 3. Building the operators
     *
     * @param stream If there is a specific stream the built [Enumerator] should process.
     * @return A [List] of terminal [Operator.Sink], ready to be processed.
     */
    @Suppress("UNCHECKED_CAST")
    fun build(stream: Stream<*>? = null): List<Operator.Sink<Retrievable>> {
        return parseOperations().map { root ->

            val config = root.opConfig as? OperatorConfig.Enumerator ?: throw IllegalArgumentException("Root stage must always be an enumerator!")
            val built = HashMap<String, Operator<*>>()
            root.opName ?: throw IllegalArgumentException("Root stage cannot be passthrough!")
            built[root.name] = buildEnumerator(root.opName, config, stream)

            for (output in root.output) {
                buildInternal(output, built)
            }

            /* Built terminal stage. */
            val output = this.config.output.map { (built[it] as? Operator<Retrievable>) ?: throw IllegalArgumentException("Output operation $it not found in pipeline!") }
            if (output.isEmpty()) throw IllegalStateException("No output operators found in pipeline!")
            if (output.size == 1) {
                DefaultSink(output.first(),"output")
            } else {
                DefaultSink(when (this.config.mergeType) {
                    MERGE -> MergeOperator(output)
                    COMBINE -> CombineOperator(output)
                    CONCAT -> ConcatOperator(output)
                    null -> throw IllegalStateException("Merge type must be specified if multiple outputs are defined.")
                }, "output")
            }
        }
    }

    /**
     * This is an internal function that can be called recursively to build the [Operator] DAG.
     *
     * @param operation The [BaseOperation] to build.
     * @param memoizationTable The memoization table that holds the already built operators.
     * @return The built [Operator].
     */
    private fun buildInternal(operation: Operation, memoizationTable: MutableMap<String, Operator<*>>, breakAt: Operation? = null) {
        /* Find all required input operations and merge them (if necessary). */
        if (operation == breakAt) return
        val inputs = operation.input.map {
            if (memoizationTable[it.name] == null) {
                buildInternal(it, memoizationTable, breakAt)
            }
            memoizationTable[it.name]!!
        }
        val op = when (inputs.size) {
            0 -> throw IllegalStateException("Input of operation ${operation.name} is empty! Dangling operators are not supported.")
            1 -> inputs.first()
            else -> when (operation.merge) {
                MERGE -> MergeOperator(inputs.filterIsInstance<Operator<Retrievable>>())
                COMBINE -> CombineOperator(inputs.filterIsInstance<Operator<Retrievable>>())
                CONCAT -> ConcatOperator(inputs.filterIsInstance<Operator<Retrievable>>())
                null -> throw IllegalStateException("Merge type must be specified if multiple inputs are defined.")
            }
        }

        /* Prepare and cache operator. */
        if (operation.opName != null && operation.opConfig != null) {
            val operator = buildOperator(operation.opName, op, operation.opConfig)
            if (operation.output.size > 1) {
                memoizationTable[operation.name] = BroadcastOperator(operator)
            } else {
                memoizationTable[operation.name] = operator
            }
        } else {
            if (operation.output.size > 1) {
                memoizationTable[operation.name] = BroadcastOperator(op)
            } else {
                memoizationTable[operation.name] = op
            }
        }


        /* Process output operators. */
        for (output in operation.output) {
            buildInternal(output, memoizationTable, operation)
        }
    }

    /**
     * Parses the pipeline definition, the [IngestionConfig.operations] chain.
     *
     * @return A list of root [Operation]s that represent the pipeline definition.
     */
    fun parseOperations(): List<Operation> {
        logger.debug { "Starting building operator tree(s)" }

        /* Find operations without inputs, these are (by definition) enumerators / entry points */
        val entrypoints = this.config.operations.entries.filter { it.value.isEntry() }
        logger.debug { "Found the following entry points: $entrypoints" }

        /* Build trees with entry points as roots. */
        return entrypoints.map {
            val stages = HashMap<String, Operation>()
            it.value.operator ?: throw IllegalArgumentException("Entrypoints must have an operator!")
            val root = Operation(it.key, it.value.operator!!, config.operators[it.value.operator] ?: throw IllegalArgumentException("Undefined operator '${it.value.operator}'"), it.value.merge)
            stages[it.key] = root
            for (operation in this.config.operations) {
                if (!stages.containsKey(operation.key)) {
                    when(operation.value.operator) {
                        is String ->
                            stages[operation.key] = Operation(
                                name = operation.key,
                                opName = operation.value.operator!!,
                                opConfig = config.operators[operation.value.operator!!] ?: throw IllegalArgumentException("Undefined operator '${operation.value.operator}'"),
                                merge = operation.value.merge
                            )

                        null ->
                            stages[operation.key] = Operation(name = operation.key, opName = null, opConfig = null, merge = operation.value.merge)
                    }
                }
                for (inputKey in operation.value.inputs) {
                    if (!stages.containsKey(inputKey)) {
                        val op = this.config.operations[inputKey] ?: throw IllegalArgumentException("Undefined operation '${inputKey}'")
                        stages[inputKey] = Operation(inputKey, op.operator!!, config.operators[op.operator] ?: throw IllegalArgumentException("Undefined operator '${op.operator}'"), op.merge)
                    }
                    stages[operation.key]?.addInput(stages[inputKey]!!)
                }
            }
            root
        }.apply {
            logger.debug { "Found and build ${this.size} operation tree(s). Root(s) is / are enumerator(s)" }
        }
    }

    /**
     * Build the [Operator] based on the provided [OperatorConfig] at the specified position.
     *
     * @param name The name of the [Operator] to build.
     * @param parent The [parent [Operator].
     * @param config The [OperatorConfig] which holds information on how to build the [Operator]
     */
    @Suppress("UNCHECKED_CAST")
    private fun buildOperator(name: String, parent: Operator<*>, config: OperatorConfig): Operator<*> {
        logger.debug { "Building operator for configuration $config" }
        return when (config) { // the when-on-type is on purpose: It enforces all branches
            is OperatorConfig.Decoder -> buildDecoder(name, parent as Enumerator, config)
            is OperatorConfig.Transformer -> buildTransformer(name, parent as Operator<Retrievable>, config) // Unchecked cast SHOULD(tm) be fine due to validation of pipeline
            is OperatorConfig.Extractor -> buildExtractor(name, parent as Operator<Retrievable>, config) // Unchecked cast SHOULD(tm) be fine due to validation of pipeline
            is OperatorConfig.Exporter -> buildExporter(name, parent as Operator<Retrievable>, config) // Unchecked cast SHOULD(tm) be fine due to validation of pipeline
            else -> throw IllegalStateException("Operator type $config is not supported in this context!")
        }
    }

    /**
     * Parses and builds the [Enumerator] based on the given [OperatorConfig.Enumerator].
     *
     * @param name The name of the [Enumerator] to build.
     * @param config The [OperatorConfig.Enumerator] which holds information on how to build the [Enumerator].
     * @param stream Optional [Stream] the [Enumerator] should process.
     * @return The built [Enumerator].
     */
    private fun buildEnumerator(name: String, config: OperatorConfig.Enumerator, stream: Stream<*>? = null): Enumerator {
        val factory = loadFactory<EnumeratorFactory>(config.factory)
        return if (stream == null) {
            factory.newEnumerator(name, context, config.mediaTypes)
        } else {
            factory.newEnumerator(name, context, config.mediaTypes, stream)
        }.apply {
            logger.info {
                "Instantiated new ${
                    if (stream != null) {
                        "stream-input"
                    } else {
                        ""
                    }
                } Enumerator: ${this.javaClass.name}"
            }
        }
    }

    /**
     * Parses and builds the [Decoder] based on the given [OperatorConfig.Enumerator].
     *
     * @param name The name of the [Decoder] to build.
     * @param parent The parent [Enumerator] that acts as input to the [Decoder].
     * @param config The [OperatorConfig.Enumerator] which holds information on how to build the [Enumerator].
     * @return The built [Enumerator].
     */
    private fun buildDecoder(name: String, parent: Enumerator, config: OperatorConfig.Decoder): Decoder {
        val factory = loadFactory<DecoderFactory>(config.factory)
        return factory.newDecoder(name, parent, context).apply {
            logger.info { "Instantiated new Decoder: ${this.javaClass.name}" }
        }
    }

    /**
     * Builds a [Transformer] based on the [OperatorConfig.Transformer]'s factory.
     *
     * @param name The name of the [Transformer]
     * @param parent The preceding parent [Operator]s.
     * @param config The [OperatorConfig.Transformer] describing the to-be-built [Transformer]
     */
    private fun buildTransformer(name: String, parent: Operator<Retrievable>, config: OperatorConfig.Transformer): Transformer {
        val factory = loadFactory<TransformerFactory>(config.factory)
        return factory.newTransformer(name, parent, context).apply {
            logger.info { "Built transformer: ${this.javaClass.name} with name $name" }
        }
    }

    /**
     * Builds an [Exporter] based on the [OperatorConfig.Exporter]'s factory OR the [OperatorConfig.Exporter.exporterName] named [Exporter] in the [Schema].
     *
     * @param name The name of the [Exporter]
     * @param parent The preceding parent [Operator]s.
     * @param config: The [OperatorConfig.Exporter] describing the to-be-built [Exporter]
     */
    private fun buildExporter(name: String, parent: Operator<Retrievable>, config: OperatorConfig.Exporter): Exporter {
        return if (!config.factory.isNullOrBlank()) {
            /* Case factory is specified */
            val factory = loadFactory<ExporterFactory>(config.factory)
            factory.newExporter(name, parent, context).apply {
                logger.info { "Built exporter from factory: ${config.factory}." }
            }
        } else if (!config.exporterName.isNullOrBlank()) {
            /* Case exporter name is given. Due to require in ExporterConfig.init, this is fine as an if-else */
            val exporter = context.schema.getExporter(config.exporterName) ?: throw IllegalArgumentException("Exporter '${config.exporterName}' does not exist on schema '${context.schema.name}'")
            exporter.getExporter(parent, context).apply {
                logger.info { "Built exporter by name from schema: ${config.exporterName}." }
            }
        } else {
            throw IllegalStateException("OperatorConfig.Exporter must have either a exporter name or a factory name specified!")
        }
    }

    /**
     * Builds an [Extractor] based on the [OperatorConfig.Extractor.fieldName] or the [OperatorConfig.Extractor.factory] named field in the schema.
     *
     * @param parent The preceding [Operator]. Has to be one of: [Exporter], [Extractor].
     * @param config: The [OperatorConfig.Extractor] describing the to-be-built [Extractor]
     */
    private fun buildExtractor(name: String, parent: Operator<Retrievable>, config: OperatorConfig.Extractor): Extractor<*, *> {
        if (!config.fieldName.isNullOrBlank()) {
            val field = this.context.schema[config.fieldName] ?: throw IllegalArgumentException("Field '${config.fieldName}' does not exist in schema '${context.schema.name}'")
            return field.getExtractor(parent, this.context).apply {
                logger.info { "Built extractor by name field name: ${config.fieldName}" }
            }
        } else if (!config.factory.isNullOrBlank()) {
            val factory = loadFactory<Analyser<ContentElement<*>, Descriptor<*>>>(config.factory)
            return factory.newExtractor(name, parent, this.context).apply {
                logger.info { "Built extractor by factory: ${config.factory}" }
            }
        } else {
            throw IllegalStateException("OperatorConfig.Extractor must have either a field name or a factory name specified!")
        }
    }

    /**
     * Loads the appropriate factory for the given name.
     *
     * @param name The (fully qualified or simple) class name of a factory.
     * @throws IllegalArgumentException If the class could not be found: Either the simple name is not unique or there exists no such class.
     */
    private inline fun <reified T : Any> loadFactory(name: String): T {
        return loadServiceForName<T>(name) ?: throw IllegalArgumentException("Failed to find '${T::class.java.simpleName}' implementation for name '$name'")
    }
}
