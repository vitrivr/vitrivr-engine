package org.vitrivr.engine.core.operators.retrieve

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable

interface Aggregator {

    fun aggregate(flows: Iterable<Flow<ScoredRetrievable>>) : Flow<ScoredRetrievable>

}