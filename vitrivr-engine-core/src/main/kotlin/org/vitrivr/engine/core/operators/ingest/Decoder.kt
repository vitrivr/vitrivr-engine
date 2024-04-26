package org.vitrivr.engine.core.operators.ingest

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.Source

/**
 * An [Operator.Unary] that converts a [Flow] of [Source] into a [Flow] of [Content] elements.
 *
 * Implementations of this class act as data source for an content extraction and ingest pipeline.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
interface Decoder : Operator.Unary<Source, Ingested>