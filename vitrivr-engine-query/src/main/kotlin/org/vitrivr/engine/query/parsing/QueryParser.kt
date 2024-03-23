package org.vitrivr.engine.query.parsing

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.AggregatorFactory
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.operators.retrieve.Transformer
import org.vitrivr.engine.core.operators.retrieve.TransformerFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.query.execution.RetrievedLookup
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.input.InputType
import org.vitrivr.engine.query.model.api.input.RetrievableIdInputData
import org.vitrivr.engine.query.model.api.input.VectorInputData
import org.vitrivr.engine.query.model.api.operator.AggregatorDescription
import org.vitrivr.engine.query.model.api.operator.RetrieverDescription
import org.vitrivr.engine.query.model.api.operator.TransformerDescription
import java.util.*

/**
 * A class that parses an [InformationNeedDescription] and transforms it into an [Operator]
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
class QueryParser(val schema: Schema) {

    /**
     * Parses an [InformationNeedDescription] and returns an [Operator] that represents the query.
     *
     * @param description [InformationNeedDescription] to parse.
     * @return The output [Operator] of the query.
     */
    fun parse(description: InformationNeedDescription): Operator<Retrieved> {
        val operators = mutableMapOf<String, Operator<Retrieved>>()
        val contentCache = mutableMapOf<String, ContentElement<*>>()

        /* Parse individual operators and append the to the operators map. */
        description.operations.forEach { (operationName, operationDescription) ->
            operators[operationName] = when (operationDescription) {
                is RetrieverDescription -> parseRetrieverOperator(description, operationName, contentCache)
                is TransformerDescription -> parseTransformationOperator(description, operationName, operators)
                is AggregatorDescription -> parseAggregationOperator(description, operationName, operators)
            }
        }

        /* Return the output operator. */
        return operators[description.output] ?: throw IllegalArgumentException("Output operation '${description.output}' is not defined.")
    }

    /**
     * Parses a named [Operator] form a [InformationNeedDescription] and returns it. Typically, this method returns a [Retriever].
     *
     * @param description [InformationNeedDescription] to parse.
     * @param operatorName Name of the operator to parse.
     * @param content Map of (cached) [ContentElement]s.
     *
     * @return [Operator] instance.
     */
    private fun parseRetrieverOperator(description: InformationNeedDescription, operatorName: String, content: MutableMap<String, ContentElement<*>>): Operator<Retrieved> {
        /* Extract necessary information. */
        val operation = description.operations[operatorName] as? RetrieverDescription ?: throw IllegalArgumentException("Operation '$operatorName' not found in information need description.")
        val input = description.inputs[operation.input] ?: throw IllegalArgumentException("Input '${operation.input}' for operation '$operatorName' not found")
        val field = this.schema[operation.field] ?: throw IllegalArgumentException("Retriever '${operation.field}' not defined in schema")

        /* Special case: handle pass-through. */
        if (operation.field.isEmpty()) { //special case, handle pass-through
            require(input.type == InputType.ID) { "Only inputs of type ID are supported for direct retrievable lookup" }
            return RetrievedLookup(this.schema.connection.getRetrievableReader(), listOf(UUID.fromString((input as RetrievableIdInputData).id)))
        }

        /* Generate retriever instance. */
        return when (input) {
            is VectorInputData -> { /* TODO: Not very happy with this, since this requires a bit too much knowledge about the schema. */
                /* Prepare query parameters. */
                val k = description.context.getProperty(field.fieldName, "limit")?.toIntOrNull() ?: 1000
                val fetchVector = description.context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false
                val distance = description.context.getProperty(field.fieldName, "distance")?.let { Distance.valueOf(it) } ?: Distance.EUCLIDEAN
                val vector = input.data.map { Value.Float(it) }

                /* Prepare query. */
                val query = ProximityQuery(value = vector, k = k, fetchVector = fetchVector, distance = distance)
                field.getRetrieverForQuery(query, description.context)
            }

            is RetrievableIdInputData -> { /* TODO: Not very happy with this either; I would argue that MLT should be a Query primitive in this c ase. */
                val id = UUID.fromString(input.id)
                val reader = field.getReader()
                val descriptor = reader.getBy(id, "retrievableId") ?: throw IllegalArgumentException("No retrievable with id '$id' present in ${field.fieldName}")
                require(descriptor is FloatVectorDescriptor) { "Descriptor is not a FloatVectorDescriptor." }

                /* Prepare query parameters. */
                val k = description.context.getProperty(field.fieldName, "limit")?.toIntOrNull() ?: 1000
                val fetchVector = description.context.getProperty(field.fieldName, "returnDescriptor")?.toBooleanStrictOrNull() ?: false
                val distance = description.context.getProperty(field.fieldName, "distance")?.let { Distance.valueOf(it) } ?: Distance.EUCLIDEAN

                /* Prepare query. */
                val query = ProximityQuery(value = descriptor.vector, k = k, fetchVector = fetchVector, distance = distance)
                field.getRetrieverForQuery(query, description.context)
            }

            else -> { /* Handles all content input. */
                val c = content.computeIfAbsent(operation.input) { input.toContent() }
                field.getRetrieverForContent(c, description.context)
            }
        }
    }

    /**
     * Parses a named [Operator] form a [InformationNeedDescription] and returns it. Typically, this method returns a [Transformer].
     *
     * @param description [InformationNeedDescription] to parse.
     * @param operatorName Name of the operator to parse.
     * @param operators Map of existing (i.e., parsed) [Operator]s.
     *
     * @return [Operator] instance.
     */
    private fun parseTransformationOperator(description: InformationNeedDescription, operatorName: String, operators: Map<String, Operator<Retrieved>>): Operator<Retrieved> {
        val operation = description.operations[operatorName] as? TransformerDescription ?: throw IllegalArgumentException("Operation '$operatorName' not found in information need description.")
        val input = operators[operation.input] ?: throw IllegalArgumentException("Input '${operation.input}' for operation '$operatorName' not found")
        val factory = loadServiceForName<TransformerFactory>(operation.transformerName + "Factory")
            ?: throw IllegalArgumentException("No factory found for '${operation.transformerName}'")
        return factory.newTransformer(input, schema, operation.properties)
    }

    /**
     * Parses a named [Operator] form a [InformationNeedDescription] and returns it. Typically, this method returns a [Transformer].
     *
     * @param description [InformationNeedDescription] to parse.
     * @param operatorName Name of the operator to parse.
     * @param operators Map of existing (i.e., parsed) [Operator]s.
     *
     * @return [Operator] instance.
     */
    private fun parseAggregationOperator(description: InformationNeedDescription, operatorName: String, operators: Map<String, Operator<Retrieved>>): Operator<Retrieved> {
        val operation = description.operations[operatorName] as? AggregatorDescription ?: throw IllegalArgumentException("Operation '$operatorName' not found in information need description.")
        require(operation.inputs.isNotEmpty()) { "Inputs of an aggregation operator cannot be empty." }

        /* Extract input operators from operators map. */
        val inputs = operation.inputs.map {
            operators[it] ?: throw IllegalArgumentException("Operator '$it' not yet defined")
        }

        /* Create aggregation operator. */
        val factory = loadServiceForName<AggregatorFactory<Retrieved, Retrieved>>(operation.aggregatorName + "Factory") ?: throw IllegalArgumentException("No factory found for '${operation.aggregatorName}'")
        return factory.newAggregator(inputs, schema, operation.properties)
    }
}