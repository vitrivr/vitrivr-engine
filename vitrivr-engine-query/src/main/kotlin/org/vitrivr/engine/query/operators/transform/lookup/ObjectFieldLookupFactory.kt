package org.vitrivr.engine.query.operators.transform.lookup

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory


/**
 * [TransformerFactory] for [ObjectFieldLookup].
 *
 * @version 1.0.0
 * @author Ralph Gasser
 */
class ObjectFieldLookupFactory() : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): ObjectFieldLookup {
        require(context is QueryContext)
        val field = context[name, "field"] ?: throw IllegalArgumentException("Expected 'field' to be defined in properties")
        val predicates = context[name, "predicates"]?.split(",")?.toSet() ?: emptySet()
        val reader = (context.schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")).getReader()
        return ObjectFieldLookup(input, reader, predicates, name)
    }
}