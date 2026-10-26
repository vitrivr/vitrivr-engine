package org.vitrivr.engine.query.operators.retrieve

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.query.filter.MetadataFilter
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

/** Enumerates matching IDs for Boolean combinations without a CLIP/text/first-person anchor. */
class FilteredRetrievalFactory : OperatorFactory {
    override fun newOperator(name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context): Operator<Retrieved> {
        require(inputs.isEmpty()) { "FilteredRetrieval has no input operators." }
        val filter = MetadataFilter.fromParameters(context.global + context.local[name].orEmpty(), context.filters[name])
            ?: throw IllegalArgumentException("FilteredRetrieval requires an early filter.")
        val limit = context[name, "limit"]?.let {
            it.toIntOrNull() ?: throw IllegalArgumentException("limit must be a positive integer.")
        } ?: 1000
        require(limit > 0) { "limit must be positive." }
        val retrievableType = context[name, "retrievableType"]
        require(retrievableType == null || retrievableType.isNotBlank()) { "retrievableType must not be blank." }
        val expression = filter.asExpression()
        expression.validate()
        return object : Operator.Nullary<Retrieved> {
            override val name = name
            override fun toFlow(scope: CoroutineScope) = flow {
                context.schema.connection.getRetrievableReader().queryFiltered(context.schema, expression, limit, retrievableType)
                    .forEach { emit(it) }
            }
        }
    }
}
