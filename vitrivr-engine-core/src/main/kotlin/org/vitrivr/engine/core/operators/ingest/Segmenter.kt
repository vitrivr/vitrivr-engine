package org.vitrivr.engine.core.operators.ingest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.BroadcastingOperator
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that segments a [Flow] of [Content] to a [Flow] of [Ingested]s.
 *
 * The [Segmenter] enforces a specific segmentation of the [Content] flow, must not be 1-to-1. Multiple downstream [Operator] pipelines
 * may be consuming the resulting [SharedFlow]
 *
 * It is not recommended to implement this interface directly. Instead, extend the [AbstractSegmenter] class.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Segmenter : Operator.Unary<Content, Ingested>, BroadcastingOperator<Ingested>