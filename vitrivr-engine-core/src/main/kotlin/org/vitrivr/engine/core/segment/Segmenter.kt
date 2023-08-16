package org.vitrivr.engine.core.segment

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.data.retrievable.Retrievable

interface Segmenter {

    fun segment() : Flow<Retrievable>

}