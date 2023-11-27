package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithScore
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that transforms, filters, or enriches a [Flow] of [RetrievableWithScore] in any conceivable way.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Transformer<I : Retrieved, O : Retrieved> : Operator.Unary<I, O>