package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that transforms, filters, or enriches a [Flow] of [Retrieved] in any conceivable way.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Transformer : Operator.Unary<Retrieved, Retrieved>