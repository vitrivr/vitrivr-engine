package org.vitrivr.engine.core.extract

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.data.retrievable.Retrievable
import org.vitrivr.engine.core.describe.Describer

interface Extractor : Describer {

    fun extract() : Flow<Retrievable>

}