package org.vitrivr.engine.query.execution

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.input.InputType
import org.vitrivr.engine.query.model.api.input.VectorInputData
import org.vitrivr.engine.query.model.api.operator.OperatorType
import org.vitrivr.engine.query.model.api.operator.RetrieverDescription

class RetrievalRuntime {

    fun query(schema: Schema, informationNeed: InformationNeedDescription): List<ScoredRetrievable> {

        val operators = mutableMapOf<String, Operator<ScoredRetrievable>>()
        val contentCache = mutableMapOf<String, Content>()

        informationNeed.operations.forEach { (operationName, operationDescription) ->
            when (operationDescription.type) {
                OperatorType.RETRIEVER -> {
                    operationDescription as RetrieverDescription

                    val field = schema.getField(operationName)
                        ?: throw IllegalArgumentException("Retriever '$operationName' not defined in schema")

                    val inputDescription = informationNeed.inputs[operationDescription.input]
                        ?: throw IllegalArgumentException("Input '${operationDescription.input}' for operation '$operationName' not found")

                    val retriever = if (inputDescription.type == InputType.VECTOR) {
                        inputDescription as VectorInputData

                        val descriptor = FloatVectorDescriptor(
                            transient = true,
                            vector = inputDescription.data
                        )

                        field.getRetriever(descriptor) //FIXME cast exception?
                    } else {
                        val cachedContent = contentCache[operationDescription.input]
                        val content = if (cachedContent != null) {
                            cachedContent
                        } else {
                            val newContent = inputDescription.toContent()
                            contentCache[operationDescription.input] = newContent
                            newContent
                        }
                        field.getRetriever(content)
                    }

                    operators[operationName] = retriever

                }

                OperatorType.TRANSFORMER -> TODO()
                OperatorType.AGGREGATOR -> TODO()
            }
        }

        val outputOperator = operators[informationNeed.output]
            ?: throw IllegalArgumentException("Output operation '${informationNeed.output}' not defined")



        return runBlocking {
            outputOperator.toFlow(this).toList()
        }


    }

}