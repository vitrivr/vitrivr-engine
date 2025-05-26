package org.vitrivr.engine.query.parsing

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.factory.InMemoryContentFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName
import org.vitrivr.engine.query.model.api.InformationNeedDescription

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
    fun parse(description: InformationNeedDescription): Operator<out Retrievable> {
        /* Prepare query context. */
        val context = Context(
            schema = this.schema,
            contentFactory = InMemoryContentFactory().newContentFactory(this.schema, emptyMap()),
            local = description.operations.map { (name, description) -> name to description.parameters }.toMap()
        )

        val operators = mutableMapOf<String, Operator<out Retrievable>>()
        val contentCache = mutableMapOf<String, ContentElement<*>>()

        /* Parse individual operators and append the operators map. */
        description.operations.forEach { (operationName, operationDescription) ->

            if (operationDescription.field != null) { //must be a retriever

                val fieldAndAttributeName: Pair<String,String?> = if (operationDescription.field.contains(".")) {
                    val f = operationDescription.field.substringBefore(".")
                    val a = operationDescription.field.substringAfter(".")
                    f to a
                } else {
                    operationDescription.field to null
                }
                val field = this.schema[fieldAndAttributeName.first] ?: throw IllegalArgumentException("Retriever '${operationDescription.field}' not defined in schema")

                val inputs = operationDescription.inputs.map { (k, v) ->
                    val inputData = (description.inputs[v] ?: throw IllegalArgumentException("Input '${v}' for operation '$operationName' not found"))

                    k to inputData.toContent()

                }.toMap()

                operators[operationName] = field.getRetrieverForContent(inputs, context)

                return@forEach

            }

            //TODO find good way to handle retriever not bound to a field

            val factory = loadServiceForName<OperatorFactory>(operationDescription.className + "Factory") ?: throw IllegalArgumentException("No factory found for '${operationDescription.className}'")

            val inputs = operationDescription.inputs.map { (k, v) ->
                k to (operators[v] ?: throw IllegalArgumentException("Operator '$v' not yet defined"))
            }.toMap()


            operators[operationName] = factory.newOperator(operationDescription.name, inputs, context)
        }

        /* Return the output operator. */
        return operators[description.output] ?: throw IllegalArgumentException("Output operation '${description.output}' is not defined.")
    }


}
