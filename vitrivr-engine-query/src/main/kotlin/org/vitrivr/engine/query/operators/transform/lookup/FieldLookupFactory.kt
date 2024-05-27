package org.vitrivr.engine.query.operators.transform.lookup

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory

class FieldLookupFactory() : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): FieldLookup {
        require(context is QueryContext)
        val keys = context[name, "keys"]?.split(",")?.map { s -> s.trim() } ?: emptyList()
        val field = context[name, "field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val reader = (context.schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")).getReader()
        return FieldLookup(input, reader, keys)
    }
}
