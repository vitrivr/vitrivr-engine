package org.vitrivr.engine.query.operators.transform.lookup

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * [TransformerFactory] for the [FieldLookup] operator.
 *
 * @version 1.1.0
 * @author Luca Rossetto
 */
class FieldLookupFactory : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): FieldLookup {
        require(context is QueryContext)
        val field = context[name, "field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val schemaField = context.schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")
        val reader = schemaField.getReader()
        return FieldLookup(input, reader, name)
    }
}
