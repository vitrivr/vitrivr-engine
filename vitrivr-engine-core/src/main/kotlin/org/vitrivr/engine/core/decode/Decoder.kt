package org.vitrivr.engine.core.decode

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.data.content.Content

interface Decoder {

    fun decode() : Flow<Content>

}