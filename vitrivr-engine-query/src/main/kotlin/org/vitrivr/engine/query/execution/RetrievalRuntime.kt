package org.vitrivr.engine.query.execution

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Aggregator
import org.vitrivr.engine.core.operators.retrieve.Transformer
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

                    val field = schema[operationName]
                        ?: throw IllegalArgumentException("Retriever '$operationName' not defined in schema")

                    val inputDescription = informationNeed.inputs[operationDescription.input]
                        ?: throw IllegalArgumentException("Input '${operationDescription.input}' for operation '$operationName' not found")

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
                            val descriptor = reader[id] ?: throw IllegalArgumentException("No retrievable with id '$id' present in ${field.fieldName}")

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

                    val transformer: Transformer<Retrieved, Retrieved> =
                        TODO("TODO: get transformer based on name and initialize with input and properties")

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

                    val aggreagtor: Aggregator = TODO()

                    operators[operationName] = aggreagtor
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