package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

/**
 * Supplies persisted shot IDs to ingestion fields that resolve their own media.
 *
 * @author Rahel Arnold
 */
class StoredShots : OperatorFactory {
    override fun newOperator(
        name: String, inputs: Map<String, Operator<out Retrievable>>, context: Context
    ): Operator<Retrievable> {
        require(inputs.isEmpty()) { "StoredShots must be the first pipeline operation." }
        val shotType = context[name, "shotType"] ?: "SEGMENT"
        return object : Operator.Nullary<Retrievable> {
            override val name = name

            override fun toFlow(scope: CoroutineScope) = flow {
                val reader = context.schema.connection.getRetrievableReader()
                for (shot in reader.getAll().filter { it.type == shotType }) emit(shot)
                emit(TerminalRetrievable)
            }.flowOn(Dispatchers.IO)
        }
    }
}
