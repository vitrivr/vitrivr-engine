package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.ContextFactory
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
class IngestionPipelineBuilder(private val config: IngestionConfig, private val context: Context) {
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
        return parseOperations().map { root ->
            val config = root.opConfig ?: throw IllegalArgumentException("Root stage must always be an enumerator!")
            val built = HashMap<String, Operator<out Retrievable>>()
            root.opName ?: throw IllegalArgumentException("Root operator must be backed by a factory!")
            val factory = loadFactory<OperatorFactory>(root.opName)
            built[root.name] = factory.newOperator(root.opName, this.context)

            for (output in root.output) {
                buildInternal(output, built)
            }

            /* Built terminal stage. */
            val output = this.config.output.map {
                (built[it] as? Operator<Retrievable>)
                    ?: throw IllegalArgumentException("Output operation $it not found in pipeline!")
            }
            if (output.isEmpty()) throw IllegalStateException("No output operators found in pipeline!")
            if (output.size == 1) {
                DefaultSink(output.first(), "output")
            } else {
                DefaultSink(
                    when (this.config.mergeType) {
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
    private fun buildInternal(operation: Operation, memoizationTable: MutableMap<String, Operator<out Retrievable>>, breakAt: Operation? = null) {
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
            val root = Operation(
                it.key,
                it.value.operator!!,
                config.operators[it.value.operator]
                    ?: throw IllegalArgumentException("Undefined operator '${it.value.operator}'"),
                it.value.merge
            )
            stages[it.key] = root
            for (operation in this.config.operations) {
                if (!stages.containsKey(operation.key)) {
                    when (operation.value.operator) {
                        is String ->
                            stages[operation.key] = Operation(
                                name = operation.key,
                                opName = operation.value.operator!!,
                                opConfig = config.operators[operation.value.operator!!]
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
                        val op = this.config.operations[inputKey]
                            ?: throw IllegalArgumentException("Undefined operation '${inputKey}'")
                        stages[inputKey] = Operation(
                            inputKey,
                            op.operator!!,
                            config.operators[op.operator]
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
     */
    private fun buildOperator(name: String, parent: Operator<out Retrievable>, config: OperatorConfig): Operator<out Retrievable> {
        logger.debug { "Building operator for configuration: $config" }
        return when {
            config.field != null -> buildExtractorForField(parent as Operator<Retrievable>, config.field, config.parameters)
            config.exporter != null -> buildExporterForName(parent as Operator<Retrievable>, config.exporter, config.parameters)
            config.factory != null -> buildOperatorForFactory(name, parent, config.factory, config.parameters)
            else -> throw IllegalStateException("Operator $config is missing required fields!")
        }
    }

    /**
     * Build the [Operator] based on the provided [OperatorConfig] at the specified position.
     *
     * @param name The name of the [Operator] to build.
     * @param parent The parent [Operator].
     * @param config The [OperatorConfig] which holds information on how to build the [Operator]
     */
    private fun buildOperatorForFactory(name: String, parent: Operator<out Retrievable>, factoryName: String, parameters: Map<String, String> = emptyMap()): Operator<out Retrievable> {
        val factory = loadFactory<OperatorFactory>(factoryName)
        return factory.newOperator(name, parent, this.context)
    }

    /**
     * Builds an [Exporter] based on the [exporterName] in the [Schema].
     *
     * @param parent The preceding parent [Operator]s.
     * @param exporterName: The name of the exporter.
     * @param parameters Map of named parameters
     */
    private fun buildExporterForName(parent: Operator<Retrievable>, exporterName: String, parameters: Map<String, String> = emptyMap()): Exporter {
        /* Case exporter name is given. Due to require in ExporterConfig.init, this is fine as an if-else */
        val exporter = context.schema.getExporter(exporterName)
            ?: throw IllegalArgumentException("Exporter '${exporterName}' does not exist on schema '${context.schema.name}'")
        return exporter.getExporter(parent, parameters, this.context)
    }

    /**
     * Builds an [Extractor] based on the [fieldName] in the schema.
     *
     * @param parent The preceding [Operator]. Has to be one of: [Exporter], [Extractor].
     * @param fieldName Name of the [Schema.Field]
     * @param parameters Map of named parameters
     */
    private fun buildExtractorForField(parent: Operator<Retrievable>, fieldName: String, parameters: Map<String, String> = emptyMap()): Extractor<*, *> {
        val field = this.context.schema[fieldName] ?: throw IllegalArgumentException("Field '${fieldName}' does not exist in schema '${context.schema.name}'")
        return field.getExtractor(parent, this.context)
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
