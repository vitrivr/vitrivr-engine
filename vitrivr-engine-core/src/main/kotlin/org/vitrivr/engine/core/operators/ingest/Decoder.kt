package org.vitrivr.engine.core.operators.ingest

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that converts a [Flow] of [Source] into a [Flow] of [Content] elements.
 *
 * Implementations of this class act as data source for an content extraction and ingest pipeline.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Decoder: Operator.Unary<Source,Content> {

}