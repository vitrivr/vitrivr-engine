package org.vitrivr.engine.core.retrieve

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.data.retrievable.ScoredRetrievable

interface Retriever {

    fun retrieve() : Flow<ScoredRetrievable>

}