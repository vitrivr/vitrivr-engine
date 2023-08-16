package org.vitrivr.engine.core.transform

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.data.content.Content

interface ContentTransformer {

    fun transform(flow: Flow<Content>) : Flow<Content>

}