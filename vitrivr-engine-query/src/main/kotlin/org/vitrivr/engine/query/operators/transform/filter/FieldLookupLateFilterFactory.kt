package org.vitrivr.engine.query.operators.transform.filter

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory

class FieldLookupLateFilterFactory() : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, schema: Schema, properties: Map<String, String>): FieldLookupLateFilter {
        val field = properties["field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val comparison = properties["comparison"] ?: throw IllegalArgumentException("expected 'comparison' to be defined in properties")
        val value = properties["value"] ?: throw IllegalArgumentException("expected 'value' to be defined in properties")
        val schemaField = schema[field] ?: throw IllegalArgumentException("Field '$field' not defined in schema")
        val append = properties["append"].toBoolean()
        val limit = properties["limit"]?.toInt() ?: Int.MAX_VALUE
        val reader = schemaField.getReader()
        val providedKeys = properties["keys"]
        val keys = if(providedKeys?.isNotBlank() == true){
            if(providedKeys.length == 1 && providedKeys == "*") {
                schemaField.analyser.prototype(schemaField).layout().map { it.name }
            }else{
                providedKeys.split(",").map { s -> s.trim() }
            }
        }else{
            emptyList()
        }
        return FieldLookupLateFilter(input, reader, keys, ComparisonOperator.fromString(comparison), value, append, limit,  name)
    }
}
