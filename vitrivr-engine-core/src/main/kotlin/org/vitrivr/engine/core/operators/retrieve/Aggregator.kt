package org.vitrivr.engine.core.operators.retrieve

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.database.retrievable.RetrievableWithScore
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

interface Aggregator : Operator.Binary<Retrieved, Retrieved> {

    fun aggregate(flows: Iterable<Flow<RetrievableWithScore>>): Flow<RetrievableWithScore>

}