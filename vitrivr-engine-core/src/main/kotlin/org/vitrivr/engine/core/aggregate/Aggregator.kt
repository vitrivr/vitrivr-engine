package org.vitrivr.engine.core.aggregate

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.data.retrievable.ScoredRetrievable

interface Aggregator {

    fun aggregate(flows: Iterable<Flow<ScoredRetrievable>>) : Flow<ScoredRetrievable>

}