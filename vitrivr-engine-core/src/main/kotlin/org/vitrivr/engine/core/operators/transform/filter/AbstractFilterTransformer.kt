package org.vitrivr.engine.core.operators.transform.filter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer

/**
 * Ab abstract [Transformer] implementation.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractFilterTransformer(override val input: Operator<out Retrievable>, val predicate: (Retrievable) -> Boolean) : Transformer {
    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).filter {
        this.predicate(it)
    }
}