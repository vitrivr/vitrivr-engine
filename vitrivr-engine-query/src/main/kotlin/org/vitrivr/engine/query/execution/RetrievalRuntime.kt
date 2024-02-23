package org.vitrivr.engine.query.execution

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.AggregatorFactory
import org.vitrivr.engine.core.operators.retrieve.Transformer
import org.vitrivr.engine.core.operators.retrieve.TransformerFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.input.InputType
import org.vitrivr.engine.query.model.api.input.RetrievableIdInputData
import org.vitrivr.engine.query.model.api.input.VectorInputData
import org.vitrivr.engine.query.model.api.operator.AggregatorDescription
import org.vitrivr.engine.query.model.api.operator.OperatorType
import org.vitrivr.engine.query.model.api.operator.RetrieverDescription
import org.vitrivr.engine.query.model.api.operator.TransformerDescription
import java.util.*

class RetrievalRuntime {

    fun query(schema: Schema, informationNeed: InformationNeedDescription): List<Retrieved> {

        val operators = mutableMapOf<String, Operator<Retrieved>>()
        val contentCache = mutableMapOf<String, ContentElement<*>>()

        informationNeed.operations.forEach { (operationName, operationDescription) ->
            when (operationDescription.type) {
                OperatorType.RETRIEVER -> {
                    operationDescription as RetrieverDescription

                    val inputDescription = informationNeed.inputs[operationDescription.input]
                        ?: throw IllegalArgumentException("Input '${operationDescription.input}' for operation '$operationName' not found")

                    if (operationDescription.field.isEmpty()) { //special case, handle pass-through

                        if(inputDescription.type != InputType.ID) {
                            throw IllegalArgumentException("Only inputs of type ID are supported for direct retrievable lookup")
                        }

                        operators[operationName] = RetrievedLookup(
                            schema.connection.getRetrievableReader(), listOf(UUID.fromString((inputDescription as RetrievableIdInputData).id))
                        )

                        return@forEach

                    }

                    val field = schema[operationDescription.field]
                        ?: throw IllegalArgumentException("Retriever '${operationDescription.field}' not defined in schema")


                    val retriever = when (inputDescription.type) {
                        InputType.VECTOR -> {
                            inputDescription as VectorInputData

                            val descriptor = FloatVectorDescriptor(
                                transient = true,
                                vector = inputDescription.data
                            )

                            field.getRetrieverForDescriptor(descriptor, informationNeed.context)
                        }

                        InputType.ID -> {

                            val id = UUID.fromString((inputDescription as RetrievableIdInputData).id)

                            val reader = field.getReader()
                            val descriptor = reader.getBy(id, "retrievableId")
                                ?: throw IllegalArgumentException("No retrievable with id '$id' present in ${field.fieldName}")

                            field.getRetrieverForDescriptor(descriptor, informationNeed.context)

                        }

                        else -> {
                            val cachedContent = contentCache[operationDescription.input]
                            val content = if (cachedContent != null) {
                                cachedContent
                            } else {
                                val newContent = inputDescription.toContent()
                                contentCache[operationDescription.input] = newContent
                                newContent
                            }
                            field.getRetrieverForContent(content, informationNeed.context)
                        }
                    }

                    operators[operationName] = retriever

                }

                OperatorType.TRANSFORMER -> {
                    operationDescription as TransformerDescription

                    val input = operators[operationDescription.input]
                        ?: throw IllegalArgumentException("Operator '${operationDescription.input}' not yet defined")

                    val factory =
                        loadServiceForName<TransformerFactory>(operationDescription.transformerName + "Factory")
                            ?: throw IllegalArgumentException("No factory found for '${operationDescription.transformerName}'")

                    val transformer: Transformer =
                        factory.newTransformer(input, schema, operationDescription.properties)

                    operators[operationName] = transformer

                }

                OperatorType.AGGREGATOR -> {
                    operationDescription as AggregatorDescription

                    if (operationDescription.inputs.isEmpty()) {
                        throw IllegalArgumentException("Inputs of Aggregator cannot be empty")
                    }

                    val inputs = operationDescription.inputs.map {
                        operators[it] ?: throw IllegalArgumentException("Operator '$it' not yet defined")
                    }

                    val factory =
                        loadServiceForName<AggregatorFactory<Retrieved, Retrieved>>(operationDescription.aggregatorName + "Factory")
                            ?: throw IllegalArgumentException("No factory found for '${operationDescription.aggregatorName}'")


                    val aggregator = factory.newAggregator(inputs, schema, operationDescription.properties)

                    operators[operationName] = aggregator
                }
            }
        }

        val outputOperator = operators[informationNeed.output]
            ?: throw IllegalArgumentException("Output operation '${informationNeed.output}' not defined")



        return runBlocking {
            outputOperator.toFlow(this).toList()
        }
    }

}