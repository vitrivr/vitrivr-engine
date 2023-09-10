package org.vitrivr.engine.core.operators.retrieve

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithScore

interface Aggregator {

    fun aggregate(flows: Iterable<Flow<RetrievableWithScore>>): Flow<RetrievableWithScore>

}