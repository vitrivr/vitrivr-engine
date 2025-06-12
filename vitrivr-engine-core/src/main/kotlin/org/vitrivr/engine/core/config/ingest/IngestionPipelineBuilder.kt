package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.ingest.operation.Operation
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.general.Exporter
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.operators.sinks.DefaultSink
import org.vitrivr.engine.core.operators.transform.shape.BroadcastOperator
import org.vitrivr.engine.core.operators.transform.shape.CombineOperator
import org.vitrivr.engine.core.operators.transform.shape.ConcatOperator
import org.vitrivr.engine.core.operators.transform.shape.MergeOperator
import org.vitrivr.engine.core.operators.transform.shape.MergeType.*
import org.vitrivr.engine.core.util.extension.loadServiceForName

/**  Internal [KLogger] instance for logging */
private val logger: KLogger = KotlinLogging.logger { }

/**
 * Parses the [IngestionConfig] in the context of a given [Schema] to construct an ingestion pipeline.
 *
 *  @author Loris Sauter
 *  @author Ralph Gasser
 *  @version 3.0.0
 */
class IngestionPipelineBuilder(private val config: IngestionConfig, private val  context: Context) {
    /**
     * Build the indexing based this [IngestionPipelineBuilder]'s [config].
     *
     * The involved steps are:
     * 1. Parsing of the configuration
     * 2. Validating the configuration
     * 3. Building the operators
     *
     * @return A [List] of terminal [Operator.Sink], ready to be processed.
     */
    @Suppress("UNCHECKED_CAST")
    fun build(): List<Operator.Sink<Retrievable>> {

        /* Merge operator configurations. */
        val localMap = this.context.local.toMutableMap()
        for ((name, config) in config.operators) {
            localMap.compute(name) { _, v ->
                v?.toMutableMap()?.apply {
                    this.putAll(config.parameters)
                } ?: config.parameters
            }
        }

        /* Create local context. */
        val localContext = context.copy(local = localMap)

        /* Parse configuration. */
        return this.config.parseOperations().map { root ->
            val rootName = root.opName ?: throw IllegalArgumentException("Root operator not properly specified. Missing name!")
            val rootConfig = root.opConfig ?: throw IllegalArgumentException("Root operator not properly specified. Missing configuration!")
            val built = HashMap<String, Operator<out Retrievable>>()
            val factory = loadFactory<OperatorFactory>(rootConfig.factory ?: throw IllegalArgumentException("Root operator must be backed by a factory!"))
            built[root.name] = factory.newOperator(rootName, localContext)

            for (output in root.output) {
                buildInternal(output,built, localContext)
            }

            /* Built terminal stage. */
            val output = config.output.map {
                (built[it] as? Operator<Retrievable>)
                    ?: throw IllegalArgumentException("Output operation $it not found in pipeline!")
            }
            if (output.isEmpty()) throw IllegalStateException("No output operators found in pipeline!")
            if (output.size == 1) {
                DefaultSink(output.first(), "output")
            } else {
                DefaultSink(
                    when (config.mergeType) {
                        MERGE -> MergeOperator(output)
                        COMBINE -> CombineOperator(output)
                        CONCAT -> ConcatOperator(output)
                        null -> throw IllegalStateException("Merge type must be specified if multiple outputs are defined.")
                    }, "output"
                )
            }
        }
    }

    /**
     * This is an internal function that can be called recursively to build the [Operator] DAG.
     *
     * @param operation The base [Operation] to build.
     * @param memoizationTable The memoization table that holds the already built operators.
     * @return The built [Operator].
     */
    private fun buildInternal(operation: Operation, memoizationTable: MutableMap<String, Operator<out Retrievable>>, context: Context, breakAt: Operation? = null) {
        /* Find all required input operations and merge them (if necessary). */
        if (operation == breakAt) return
        val inputs = operation.input.map {
            if (memoizationTable[it.name] == null) {
                buildInternal(it, memoizationTable, context, breakAt)
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
            val operator = buildOperator(operation.opName, op, operation.opConfig, context)
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
            buildInternal(output, memoizationTable, context, operation)
        }
    }

    /**
     * Parses the pipeline definition, the [IngestionConfig.operations] chain.
     *
     * @return A list of root [Operation]s that represent the pipeline definition.
     */
    private fun IngestionConfig.parseOperations(): List<Operation> {
        logger.debug { "Starting building operator tree(s)" }

        /* Find operations without inputs, these are (by definition) enumerators / entry points */
        val entrypoints = this.operations.entries.filter { it.value.isEntry() }
        logger.debug { "Found the following entry points: $entrypoints" }

        /* Build trees with entry points as roots. */
        return entrypoints.map {
            val stages = HashMap<String, Operation>()
            it.value.operator ?: throw IllegalArgumentException("Entrypoints must have an operator!")
            val root = Operation(
                it.key,
                it.value.operator!!,
                this.operators[it.value.operator]
                    ?: throw IllegalArgumentException("Undefined operator '${it.value.operator}'"),
                it.value.merge
            )
            stages[it.key] = root
            for (operation in this.operations) {
                if (!stages.containsKey(operation.key)) {
                    when (operation.value.operator) {
                        is String ->
                            stages[operation.key] = Operation(
                                name = operation.key,
                                opName = operation.value.operator!!,
                                opConfig = this.operators[operation.value.operator!!]
                                    ?: throw IllegalArgumentException("Undefined operator '${operation.value.operator}'"),
                                merge = operation.value.merge
                            )

                        null ->
                            stages[operation.key] = Operation(
                                name = operation.key,
                                opName = null,
                                opConfig = null,
                                merge = operation.value.merge
                            )
                    }
                }
                for (inputKey in operation.value.inputs) {
                    if (!stages.containsKey(inputKey)) {
                        val op = this.operations[inputKey]
                            ?: throw IllegalArgumentException("Undefined operation '${inputKey}'")
                        stages[inputKey] = Operation(
                            inputKey,
                            op.operator!!,
                            this.operators[op.operator]
                                ?: throw IllegalArgumentException("Undefined operator '${op.operator}'"),
                            op.merge
                        )
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
     * @param parent The parent [Operator].
     * @param config The [OperatorConfig] which holds information on how to build the [Operator]
     * @param context The [Context] to use.
     */
    private fun buildOperator(name: String, parent: Operator<out Retrievable>, config: OperatorConfig, context: Context): Operator<out Retrievable> {
        logger.debug { "Building operator for configuration: $config" }
        return when {
            config.field != null -> buildExtractorForField(parent as Operator<Retrievable>, config.field, context)
            config.exporter != null -> buildExporterForName(parent as Operator<Retrievable>, config.exporter, context)
            config.factory != null -> buildOperatorForFactory(name, parent, config.factory, context)
            else -> throw IllegalStateException("Operator $config is missing required fields!")
        }
    }

    /**
     * Build the [Operator] based on the provided [OperatorConfig] at the specified position.
     *
     * @param name The name of the [Operator] to build.
     * @param parent The parent [Operator].
     * @param factoryName The name of the [OperatorFactory]
     * @param context The [Context] to use.
     */
    private fun buildOperatorForFactory(name: String, parent: Operator<out Retrievable>, factoryName: String, context: Context): Operator<out Retrievable> {
        val factory = loadFactory<OperatorFactory>(factoryName)
        return factory.newOperator(name, parent, context)
    }

    /**
     * Builds an [Exporter] based on the [exporterName] in the [Schema].
     *
     * @param parent The preceding parent [Operator]s.
     * @param exporterName: The name of the exporter.
     * @param context The [Context] to use.
     */
    private fun buildExporterForName(parent: Operator<Retrievable>, exporterName: String, context: Context): Exporter {
        /* Case exporter name is given. Due to require in ExporterConfig.init, this is fine as an if-else */
        val exporter = context.schema.getExporter(exporterName)
            ?: throw IllegalArgumentException("Exporter '${exporterName}' does not exist on schema '${context.schema.name}'")
        return exporter.getExporter(parent, context)
    }

    /**
     * Builds an [Extractor] based on the [fieldName] in the schema.
     *
     * @param parent The preceding [Operator]. Has to be one of: [Exporter], [Extractor].
     * @param fieldName Name of the [Schema.Field]
     * @param context The [Context] to use.
     */
    private fun buildExtractorForField(parent: Operator<Retrievable>, fieldName: String, context: Context): Extractor<*, *> {
        val field = context.schema[fieldName] ?: throw IllegalArgumentException("Field '${fieldName}' does not exist in schema '${context.schema.name}'")
        return field.getExtractor(parent, context)
    }

    /**
     * Loads the appropriate factory for the given name.
     *
     * @param name The (fully qualified or simple) class name of a factory.
     * @throws IllegalArgumentException If the class could not be found: Either the simple name is not unique or there exists no such class.
     */
    private inline fun <reified T : Any> loadFactory(name: String): T
        = loadServiceForName<T>(name) ?: throw IllegalArgumentException("Failed to find '${T::class.java.simpleName}' implementation for name '$name'")
}
