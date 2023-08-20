package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.ScoredRetrievable
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Nullary] that facilitates retrieval based on a [Analyser].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Retriever<T: Descriptor>: Operator.Nullary<ScoredRetrievable> {
    /** The [Analyser] this [Retriever] implements. */
    val describer: Analyser<T>
}