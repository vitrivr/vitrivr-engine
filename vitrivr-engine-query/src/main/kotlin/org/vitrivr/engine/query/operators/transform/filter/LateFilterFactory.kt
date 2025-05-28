package org.vitrivr.engine.query.operators.transform.filter

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

class LateFilterFactory() : OperatorFactory {
    override fun newOperator(
        name: String,
        inputs: Map<String, Operator<out Retrievable>>,
        context: Context
    ): LateFilter {
        require(context is Context)
        val field =
            context[name, "field"] ?: throw IllegalArgumentException("expected 'field' to be defined in properties")
        val comparison = context[name, "comparison"]
            ?: throw IllegalArgumentException("expected 'comparison' to be defined in properties")
        val value =
            context[name, "value"] ?: throw IllegalArgumentException("expected 'value' to be defined in properties")
        val skip = context[name, "skip"].toString()
        val limit = context[name, "limit"]?.toInt() ?: Int.MAX_VALUE
        val providedKeys = context[name, "keys"]
        val keys = if (providedKeys?.isNotBlank() == true) {
            providedKeys.split(",").map { s -> s.trim() }
        } else {
            emptyList()
        }
        return LateFilter(
            inputs.values.first(),
            field,
            keys,
            ComparisonOperator fromString comparison,
            value,
            limit,
            Skip fromString skip,
            name
        )
    }
}
