package org.vitrivr.engine.query.operators.transform.lookup

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

/**
 * [TransformerFactory] for the [FieldLookup] operator.
 *
 * @version 1.1.0
 * @author Luca Rossetto
 */
class FieldLookupFactory : OperatorFactory {
    override fun newOperator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        context: Context
    ): FieldLookup {
        require(context is Context)
        val field =
            context[name, "field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val schemaField =
            context.schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")
        val reader = schemaField.getReader()
        return FieldLookup(inputs.values.first(), reader, name)
    }
}
