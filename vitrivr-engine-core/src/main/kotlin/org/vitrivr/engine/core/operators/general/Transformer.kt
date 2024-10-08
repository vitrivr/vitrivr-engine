package org.vitrivr.engine.core.operators.general

import kotlinx.coroutines.flow.Flow
import org.vitrivr.engine.core.model.content.Content
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that transforms a [Flow] of [Content].
 *
 * The [Transformer] can act on the [Content] themselves but also on the [Flow] of [Content], e.g., by sampling.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
interface Transformer : Operator.Unary<Retrievable, Retrievable>