package org.vitrivr.engine.query.parsing

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.types.Type
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.AggregatorFactory
import org.vitrivr.engine.core.operators.retrieve.Retriever
import org.vitrivr.engine.core.operators.retrieve.Transformer
import org.vitrivr.engine.core.operators.retrieve.TransformerFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.query.operators.retrieval.RetrievedLookup
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.input.*
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
        val fieldAndAttributeName: Pair<String,String?> = if(operation.field.contains(".")){
            val f = operation.field.split(".").firstOrNull() ?: throw IllegalArgumentException("Field name in dot notation FIELD.ATTRIBUTE requires both, field and attribute")
            val a = operation.field.split(".").lastOrNull() ?: throw IllegalArgumentException("Field name in dot notation FIELD.ATTRIBUTE requires both, field and attribute")
            f to a
        }else{
            operation.field to null
        }
        val field = this.schema[fieldAndAttributeName.first] ?: throw IllegalArgumentException("Retriever '${operation.field}' not defined in schema")

        /* Special case: handle pass-through. */
        if (field == null) { //special case, handle pass-through
            require(input.type == InputType.ID) { "Only inputs of type ID are supported for direct retrievable lookup." }
            return RetrievedLookup(this.schema.connection.getRetrievableReader(), listOf(UUID.fromString((input as RetrievableIdInputData).id)))
        }

        /* Generate retriever instance. */
        return when (input) {
            is RetrievableIdInputData -> {
                val id = UUID.fromString(input.id)
                val reader = field.getReader()
                val descriptor = reader.getBy(id, "retrievableId") ?: throw IllegalArgumentException("No retrievable with id '$id' present in ${field.fieldName}")
                field.getRetrieverForDescriptor(descriptor, description.context)
            }
            is VectorInputData -> field.getRetrieverForDescriptor(FloatVectorDescriptor(vector = input.data.map { Value.Float(it) }, transient = true), description.context)
            else -> {
                /* Is this a boolean sub-field query ? */
                if(input.comparison != null){
                    /* yes */
                    val subfield = field.analyser.prototype(field).schema().find { it.name == fieldAndAttributeName.second } ?: throw IllegalArgumentException("Field $field does not have a subfield with name ${fieldAndAttributeName.second}")
                    /* For now, we support not all input data */
                    val value = when(input){
                        is TextInputData -> {
                            require(subfield.type == Type.STRING){"The given sub-field ${fieldAndAttributeName.first}.${fieldAndAttributeName.second}'s type is ${subfield.type}, which is not the expexted ${Type.STRING}"}
                            Value.String(input.data)
                        }
                        is BooleanInputData -> {
                            require(subfield.type == Type.BOOLEAN){"The given sub-field ${fieldAndAttributeName.first}.${fieldAndAttributeName.second}'s type is ${subfield.type}, which is not the expexted ${Type.BOOLEAN}"}
                            Value.Boolean(input.value)
                        }
                        is NumericInputData -> {
                            when(subfield.type){
                                Type.DOUBLE -> Value.Double(input.value)
                                Type.INT -> Value.Int(input.value.toInt())
                                Type.LONG -> Value.Long(input.value.toLong())
                                Type.SHORT -> Value.Short(input.value.toInt().toShort())
                                Type.BYTE -> Value.Byte(input.value.toInt().toByte())
                                Type.FLOAT -> Value.Float(input.value.toFloat())
                                else -> throw IllegalArgumentException("Cannot work with NumericInputData $input but non-numerical sub-field $subfield")
                            }
                        }
                        is DateInputData -> {
                            require(subfield.type == Type.DATETIME){"The given sub-field ${fieldAndAttributeName.first}.${fieldAndAttributeName.second}'s type is ${subfield.type}, which is not the expexted ${Type.DATETIME}"}
                            Value.DateTime(input.parseDate())
                        }
                        else -> throw UnsupportedOperationException("Subfield query for $input is currently not supported")
                    }
                    val limit = description.context.getProperty(operatorName, "limit")?.toLong() ?: Long.MAX_VALUE
                    field.getRetrieverForQuery(
                        SimpleBooleanQuery(value, ComparisonOperator.fromString(input.comparison!!), fieldAndAttributeName.second, limit),
                        description.context)
                }else{
                    /* no */
                    field.getRetrieverForContent(content.computeIfAbsent(operation.input) { input.toContent() }, description.context)
                }
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
