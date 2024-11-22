package org.vitrivr.engine.query.operators.transform.filter

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.context.QueryContext
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory

class BooleanFilterFactory() : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): BooleanFilter {
        require(context is QueryContext)
        val field = context[name, "field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val comparison = context[name, "comparison"] ?: throw IllegalArgumentException("expected 'comparison' to be defined in properties")
        val value = context[name, "value"] ?: throw IllegalArgumentException("expected 'value' to be defined in properties")
        val schemaField = context.schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")
        val reader = schemaField.getReader()
        val providedKeys = context[name, "keys"]
        val keys = if(providedKeys?.isNotBlank() == true){
            if(providedKeys.length == 1 && providedKeys == "*") {
                schemaField.analyser.prototype(schemaField).layout().map { it.name }
            }else{
                providedKeys.split(",").map { s -> s.trim() }
            }
        }else{
            emptyList()
        }
        return BooleanFilter(input, reader, keys, ComparisonOperator.fromString(comparison), value, name)
    }
}
