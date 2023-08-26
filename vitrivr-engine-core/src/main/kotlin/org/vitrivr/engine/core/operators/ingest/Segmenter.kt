package org.vitrivr.engine.core.operators.ingest

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.BroadcastingOperator
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that segments a [Flow] of [Content] to a [Flow] of [IngestedRetrievable]s.
 *
 * The [Segmenter] enforces a specific segmentation of the [Content] flow, must not be 1-to-1.
 * Multiple downstream [Operator] pipelines may be consuming the resulting [Flow]
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Segmenter: Operator.Unary<Content, IngestedRetrievable>, BroadcastingOperator<IngestedRetrievable> {

    /**
     * The number of [IngestedRetrievable]s this [Segmenter] has already emitted
     */
    val emitted: Int

    /**
     * Indicates if the [Segmenter] has exhausted its input
     */
    val inputExhausted: Boolean

}