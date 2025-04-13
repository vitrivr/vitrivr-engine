package org.vitrivr.engine.query.operators.transform.filter

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.TransformerFactory

class LateFilterFactory() : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, schema: Schema, properties: Map<String, String>): LateFilter {
        val field = properties["field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val comparison =properties["comparison"]
            ?: throw IllegalArgumentException("expected 'comparison' to be defined in properties")
        val value =
            properties["value"] ?: throw IllegalArgumentException("expected 'value' to be defined in properties")
        val skip = properties["skip"].toString()
        val limit = properties["limit"]?.toInt() ?: Int.MAX_VALUE
        val providedKeys = properties["keys"]
        val keys = if (providedKeys?.isNotBlank() == true) {
            providedKeys.split(",").map { s -> s.trim() }
        } else {
            emptyList()
        }
        return LateFilter(input, field, keys, ComparisonOperator fromString comparison, value, limit, Skip fromString skip, name)
    }
}
