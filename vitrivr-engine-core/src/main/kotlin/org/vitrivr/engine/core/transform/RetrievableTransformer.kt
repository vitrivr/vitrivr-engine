package org.vitrivr.engine.core.transform

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.data.retrievable.ScoredRetrievable

interface RetrievableTransformer {

    fun transform(flow: Flow<ScoredRetrievable>) : Flow<ScoredRetrievable>

}