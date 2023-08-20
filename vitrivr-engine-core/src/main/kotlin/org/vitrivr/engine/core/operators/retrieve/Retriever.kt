package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable
import org.vitrivr.engine.core.operators.Describer
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Nullary] that facilitates retrieval based on a [Describer].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Retriever<D: Descriptor, T: ScoredRetrievable>: Operator.Nullary<T>, Describer<D>